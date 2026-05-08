package com.sprintify.project_service.service;

import com.sprintify.project_service.dto.*;
import com.sprintify.project_service.entity.BacklogItem;
import com.sprintify.project_service.entity.Project;
import com.sprintify.project_service.entity.enums.*;
import com.sprintify.project_service.entity.ProjectMember;
import com.sprintify.project_service.entity.Sprint;
import com.sprintify.project_service.repository.BacklogItemRepository;
import com.sprintify.project_service.repository.ProjectMemberRepository;
import com.sprintify.project_service.repository.ProjectRepository;
import com.sprintify.project_service.repository.SprintRepository; 
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BacklogItemService {

    private final BacklogItemRepository backlogItemRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final AsyncGamificationService asyncGamificationService;

    // ============ AUTHORIZATION HELPERS ============
    // These methods check if the user is a member of the project and has the required role for certain actions.
    private ProjectMember getMember(UUID userId, UUID projectId) {
        return projectMemberRepository.findByProject_IdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not a member of this project"));
    }

    private void validateTeamMember(UUID userId, UUID projectId) {
        getMember(userId, projectId);
    }

    private void validatePOorSM(UUID userId, UUID projectId) {
        ProjectMember member = getMember(userId, projectId);
        if (member.getRole() != ProjectMemberRole.PO && member.getRole() != ProjectMemberRole.SM) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only PO or SM can perform this action");
        }
    }

    private void validateDEV(UUID userId, UUID projectId) {
        ProjectMember member = getMember(userId, projectId);
        if (member.getRole() != ProjectMemberRole.DEV) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only DEV can create tasks");
        }
    }

    // ============ CREATE ============

    @Transactional
    public BacklogItemResponseDTO createBacklogItem(UUID projectId, UUID userId, CreateBacklogItemRequestDTO request) {
        // Verify project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        BacklogItem parent = null;

        // Authorization: Epic/Story → PO/SM; Task/Bug → DEV
        if (request.type() == BacklogItemType.EPIC || request.type() == BacklogItemType.USER_STORY) {
            validatePOorSM(userId, projectId);
            if(request.type() == BacklogItemType.USER_STORY && request.parentId() != null) {
                // Verify parent exists and is an Epic
                parent = backlogItemRepository.findByIdAndProject_Id(request.parentId(), projectId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent Epic not found"));
                if (parent.getType() != BacklogItemType.EPIC) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent must be an Epic");
                }
            }
        } else if (request.type() == BacklogItemType.TASK || request.type() == BacklogItemType.BUG) {
            validateDEV(userId, projectId);
            // Tasks MUST have a parent
            if (request.parentId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tasks must have a parent User Story");
            }
            // Verify parent exists and is a User Story
            parent = backlogItemRepository.findByIdAndProject_Id(request.parentId(), projectId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent User Story not found"));
            if (parent.getType() != BacklogItemType.USER_STORY) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent must be a User Story");
            }
        }

        BacklogItem item = BacklogItem.builder()
                .project(project)
                .type(request.type())
                .title(request.title().trim())
                .description(request.description() != null ? request.description().trim() : null)
                .status(BacklogItemStatus.TODO)
                .priority(request.priority() != null ? request.priority() : Priority.MEDIUM)
                .difficulty(request.difficulty() != null ? request.difficulty() : Difficulty.MEDIUM)
                .estimatedPoints(request.estimatedPoints())
                .parent(parent)
                .createdById(userId)
                .reporterId(userId)
                .backlogOrder(Integer.MAX_VALUE) // Will be ordered later
                .build();

        BacklogItem saved = backlogItemRepository.save(item);
        return toResponse(saved);
    }

    // ============ READ ============

    @Transactional(readOnly = true)
    public BacklogItemResponseDTO getBacklogItem(UUID projectId, UUID userId, UUID itemId) {
        validateTeamMember(userId, projectId);

        BacklogItem item = backlogItemRepository.findByIdAndProject_Id(itemId, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        return toResponse(item);
    }

    @Transactional(readOnly = true)
    public BacklogItemDetailResponseDTO getBacklogItemWithChildren(UUID projectId, UUID userId, UUID itemId) {
        validateTeamMember(userId, projectId);

        BacklogItem item = backlogItemRepository.findByIdAndProject_Id(itemId, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        return toDetailResponse(item);
    }

    @Transactional(readOnly = true)
    public Page<BacklogItemResponseDTO> listBacklogItems(UUID projectId, UUID userId, UUID sprintId, Pageable pageable) {
        validateTeamMember(userId, projectId);

        if (sprintId != null) {
            if (!sprintRepository.existsByIdAndProject_Id(sprintId, projectId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sprint not found in this project");
            }

            return backlogItemRepository.findAllByProject_IdAndSprint_IdOrderByBacklogOrderAsc(projectId, sprintId)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toList(),
                            list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())
                    ));
        } else {
            return backlogItemRepository.findAllBacklogItems(projectId)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toList(),
                            list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())
                    ));
        }
    }

    // ============ UPDATE ============

    @Transactional
    public BacklogItemResponseDTO updateBacklogItem(UUID projectId, UUID userId, UUID itemId, UpdateBacklogItemRequestDTO request) {
        validateTeamMember(userId, projectId);
        
        BacklogItem item = backlogItemRepository.findByIdAndProject_Id(itemId, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        // Title and Description can be updated by anyone
        if (request.title() != null) {
            item.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            item.setDescription(request.description().trim());
        }
        if (request.priority() != null) {
            item.setPriority(request.priority());
        }
        if (request.difficulty() != null) {
            item.setDifficulty(request.difficulty());
        }

        // Estimation points/hours: PO/SM only
        if (request.estimatedPoints() != null || request.estimatedHours() != null) {
            validatePOorSM(userId, projectId);
            if (request.estimatedPoints() != null) {
                item.setEstimatedPoints(request.estimatedPoints());
            }
            if (request.estimatedHours() != null) {
                item.setEstimatedHours(request.estimatedHours());
            }
        }

        BacklogItem saved = backlogItemRepository.save(item);
        return toResponse(saved);
    }

    @Transactional
    public BacklogItemResponseDTO updateStatus(UUID projectId, UUID userId, UUID itemId, UpdateBacklogItemStatusRequestDTO request) {
        validateTeamMember(userId, projectId);

        BacklogItem item = backlogItemRepository.findByIdAndProject_Id(itemId, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        BacklogItemStatus previousStatus = item.getStatus();
        item.setStatus(request.status());
        if (request.status() == BacklogItemStatus.DONE) {
            item.setCompletedAt(LocalDateTime.now());
        }

        BacklogItem saved = backlogItemRepository.save(item);

        boolean transitionedToDone = previousStatus != BacklogItemStatus.DONE && saved.getStatus() == BacklogItemStatus.DONE;
        boolean isTaskType = saved.getType() == BacklogItemType.TASK || saved.getType() == BacklogItemType.BUG;
        if (transitionedToDone && isTaskType && saved.getAssigneeId() != null) {
            asyncGamificationService.awardTaskCompletionXp(saved.getAssigneeId().toString());
        }

        return toResponse(saved);
    }

    @Transactional
    public BacklogItemResponseDTO assignItem(UUID projectId, UUID userId, UUID itemId, AssignBacklogItemRequestDTO request) {
        BacklogItem item = backlogItemRepository.findByIdAndProject_Id(itemId, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        UUID assigneeId = request == null || request.assigneeId() == null ? userId : request.assigneeId();

        // Authorization: Self-assign or PO/SM can assign anyone
        boolean isSelfAssign = assigneeId.equals(userId);
        if (!isSelfAssign) {
            validatePOorSM(userId, projectId);
        }

        // Verify assignee is a team member
        validateTeamMember(assigneeId, projectId);

        item.setAssigneeId(assigneeId);
        BacklogItem saved = backlogItemRepository.saveAndFlush(item);
        return toResponse(saved);
    }

    @Transactional
    public BacklogItemResponseDTO moveToSprint(UUID projectId, UUID userId, UUID itemId, MoveToSprintRequestDTO request) {
        validatePOorSM(userId, projectId);

        BacklogItem item = backlogItemRepository.findByIdAndProject_Id(itemId, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        if (request.sprintId() != null) {
            // Verify sprint exists and belongs to project
            // (will be done by SprintService, but we set it here)
            Sprint sprint = sprintRepository
                            .findByIdAndProject_Id(request.sprintId(), projectId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sprint not found"));

            item.setSprint(sprint);
        } else {
            // Move back to backlog
            item.setSprint(null);
        }
        BacklogItem saved = backlogItemRepository.save(item);
        return toResponse(saved);
    }

   
    // ============ DELETE ============

    @Transactional
    public void deleteBacklogItem(UUID projectId, UUID userId, UUID itemId) {
        BacklogItem item = backlogItemRepository.findByIdAndProject_Id(itemId, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        // Authorization: Creator or PO/SM
        boolean isCreator = item.getCreatedById().equals(userId);
        if (!isCreator) {
            validatePOorSM(userId, projectId);
        }

        backlogItemRepository.delete(item); // Cascade deletes children
    }

    // ============ HELPER CONVERSIONS ============

    private BacklogItemResponseDTO toResponse(BacklogItem item) {
        return new BacklogItemResponseDTO(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getStatus(),
                item.getType(),
                item.getPriority(),
                item.getDifficulty(),
                item.getEstimatedPoints(),
                item.getEstimatedHours(),
                item.getAssigneeId(),
                item.getParent() != null ? item.getParent().getId() : null,
                item.getSprint() != null ? item.getSprint().getId() : null,
                item.getCreatedById(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCompletedAt()
        );
    }

    private BacklogItemDetailResponseDTO toDetailResponse(BacklogItem item) {
        List<BacklogItemDetailResponseDTO> children = new ArrayList<>();
        if (item.getChildren() != null && !item.getChildren().isEmpty()) {
            children = item.getChildren().stream()
                    .map(this::toDetailResponse)
                    .collect(Collectors.toList());
        }

        return new BacklogItemDetailResponseDTO(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getStatus(),
                item.getType(),
                item.getPriority(),
                item.getDifficulty(),
                item.getEstimatedPoints(),
                item.getEstimatedHours(),
                item.getAssigneeId(),
                item.getParent() != null ? item.getParent().getId() : null,
                item.getSprint() != null ? item.getSprint().getId() : null,
                item.getCreatedById(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCompletedAt(),
                children
        );
    }
}
