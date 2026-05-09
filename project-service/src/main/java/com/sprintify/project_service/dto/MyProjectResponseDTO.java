package com.sprintify.project_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sprintify.project_service.entity.enums.ProjectMemberRole;

public record MyProjectResponseDTO(
        UUID id,
        String name,
        String description,
        String state,
        UUID ownerId,
        LocalDateTime createdAt,
        int memberCount,
        ProjectMemberRole currentUserRole
) {
}
