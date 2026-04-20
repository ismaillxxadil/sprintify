package com.sprintify.project_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sprintify.project_service.entity.enums.ProjectMemberRole;
import com.sprintify.project_service.entity.enums.ProjectMemberStatus;

public record InviteResponseDTO(
        UUID projectId,
        String projectName,
        ProjectMemberRole role,
        ProjectMemberStatus status,
        LocalDateTime invitedAt
) {
}
