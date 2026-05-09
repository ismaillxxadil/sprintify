package com.sprintify.project_service.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.sprintify.project_service.client.IdentityClient;
import com.sprintify.project_service.dto.CreateProjectRequestDTO;
import com.sprintify.project_service.dto.InviteDecisionRequestDTO;
import com.sprintify.project_service.dto.InviteRequestDTO;
import com.sprintify.project_service.dto.InviteResponseDTO;
import com.sprintify.project_service.dto.MyProjectResponseDTO;
import com.sprintify.project_service.dto.ProjectMemberResponseDTO;
import com.sprintify.project_service.dto.ProjectResponseDTO;
import com.sprintify.project_service.entity.Project;
import com.sprintify.project_service.entity.ProjectMember;
import com.sprintify.project_service.entity.enums.ProjectMemberRole;
import com.sprintify.project_service.entity.enums.ProjectMemberStatus;
import com.sprintify.project_service.repository.ProjectMemberRepository;
import com.sprintify.project_service.repository.ProjectRepository;

@Service
public class ProjectService {

    private static final String DEFAULT_STATE = "ACTIVE";

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final IdentityClient identityClient;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            IdentityClient identityClient
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.identityClient = identityClient;
    }

    @Transactional
    public ProjectResponseDTO createProject(CreateProjectRequestDTO request, UUID ownerId) {
        Project project = new Project();
        project.setName(request.name().trim());
        project.setDescription(request.description() == null ? null : request.description().trim());
        project.setState(normalizeState(request.state()));

        ProjectMember ownerMember = ProjectMember.builder()
                .project(project)
                .userId(ownerId)
                .role(ProjectMemberRole.PO)
            .status(ProjectMemberStatus.ACTIVE)
                .build();

        project.getMembers().add(ownerMember);
        Project savedProject = projectRepository.save(project);

        return toResponse(savedProject, ownerId);
    }

    @Transactional
    public void inviteToProject(UUID projectId, UUID requesterId, InviteRequestDTO request) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        boolean isPo = projectMemberRepository.existsByProject_IdAndUserIdAndRole(
                projectId,
                requesterId,
                ProjectMemberRole.PO
        );

        if (!isPo) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project PO can invite users");
        }

        UUID inviteeId = identityClient.getUserByEmail(request.email().trim())
                .id();

        if (projectMemberRepository.existsByProject_IdAndUserId(projectId, inviteeId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already invited or already a member");
        }

        Project projectReference = projectRepository.getReferenceById(projectId);
        ProjectMember invitedMember = ProjectMember.builder()
                .project(projectReference)
                .userId(inviteeId)
                .role(request.role())
                .status(ProjectMemberStatus.PENDING)
                .build();

        projectMemberRepository.save(invitedMember);
    }

    @Transactional(readOnly = true)
    public List<MyProjectResponseDTO> getMyProjects(UUID userId) {
        return projectMemberRepository.findAllByUserIdAndStatusOrderByJoinedAtDesc(userId, ProjectMemberStatus.ACTIVE)
                .stream()
                .map(this::toMyProjectResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InviteResponseDTO> getMyPendingInvites(UUID userId) {
        return projectMemberRepository.findAllByUserIdAndStatus(userId, ProjectMemberStatus.PENDING)
                .stream()
                .map(member -> new InviteResponseDTO(
                        member.getProject().getId(),
                        member.getProject().getName(),
                        member.getRole(),
                        member.getStatus(),
                        member.getJoinedAt()
                ))
                .toList();
    }

    @Transactional
    public void respondToInvite(UUID projectId, UUID userId, InviteDecisionRequestDTO request) {
        ProjectMember member = projectMemberRepository.findByProject_IdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invite not found"));

        if (member.getStatus() != ProjectMemberStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invite is already handled");
        }

        if (request.decision().isAccept()) {
            member.setStatus(ProjectMemberStatus.ACTIVE);
        } else {
            member.setStatus(ProjectMemberStatus.REJECTED);
        }

        projectMemberRepository.save(member);
    }

    @Transactional
    public void deleteProject(UUID projectId, UUID requesterId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        boolean isPo = projectMemberRepository.existsByProject_IdAndUserIdAndRole(
                projectId,
                requesterId,
                ProjectMemberRole.PO
        );

        if (!isPo) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project PO can delete this project");
        }

        projectRepository.delete(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponseDTO> getProjectMembers(UUID projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        return projectMemberRepository.findAllByProject_Id(projectId)
                .stream()
                .map(member -> new ProjectMemberResponseDTO(
                        member.getUserId(),
                        member.getRole(),
                        member.getStatus(),
                        member.getJoinedAt()
                ))
                .toList();
    }

    @Transactional
    public void deleteMember(UUID projectId, UUID memberId, UUID requesterId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        boolean isPo = projectMemberRepository.existsByProject_IdAndUserIdAndRole(
                projectId,
                requesterId,
                ProjectMemberRole.PO
        );

        if (!isPo) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project PO can remove members");
        }

        ProjectMember memberToDelete = projectMemberRepository.findByProject_IdAndUserId(projectId, memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found in this project"));

        if (memberToDelete.getRole() == ProjectMemberRole.PO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot remove the project owner");
        }

        projectMemberRepository.delete(memberToDelete);
    }

    private ProjectResponseDTO toResponse(Project project, UUID ownerId) {
        return new ProjectResponseDTO(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getState(),
                ownerId,
                project.getCreatedAt(),
                project.getMembers().size()
        );
    }

    private MyProjectResponseDTO toMyProjectResponse(ProjectMember member) {
        Project project = member.getProject();

        UUID ownerId = project.getMembers().stream()
                .filter(projectMember -> projectMember.getRole() == ProjectMemberRole.PO)
                .map(ProjectMember::getUserId)
                .findFirst()
                .orElse(member.getUserId());

        int memberCount = (int) project.getMembers().stream()
                .filter(projectMember -> projectMember.getStatus() == ProjectMemberStatus.ACTIVE)
                .count();

        return new MyProjectResponseDTO(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getState(),
                ownerId,
                project.getCreatedAt(),
                memberCount,
                member.getRole()
        );
    }

    private String normalizeState(String state) {
        if (state == null || state.isBlank()) {
            return DEFAULT_STATE;
        }

        return state.trim().toUpperCase();
    }
}
