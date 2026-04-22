package com.sprintify.project_service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sprintify.project_service.entity.ProjectMember;
import com.sprintify.project_service.entity.enums.ProjectMemberRole;
import com.sprintify.project_service.entity.enums.ProjectMemberStatus;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {
    boolean existsByProject_IdAndUserIdAndRole(UUID projectId, UUID userId, ProjectMemberRole role);
    boolean existsByProject_IdAndUserId(UUID projectId, UUID userId);
    List<ProjectMember> findAllByUserIdAndStatus(UUID userId, ProjectMemberStatus status);
    List<ProjectMember> findAllByProject_Id(UUID projectId);
    Optional<ProjectMember> findByProject_IdAndUserId(UUID projectId, UUID userId);
}