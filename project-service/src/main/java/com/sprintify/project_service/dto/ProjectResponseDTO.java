package com.sprintify.project_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectResponseDTO(
        UUID id,
        String name,
        String description,
        String state,
        UUID ownerId,
        LocalDateTime createdAt,
        int memberCount
) {
}