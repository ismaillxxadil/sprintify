package com.sprintify.project_service.dto;

import com.sprintify.project_service.entity.enums.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record BacklogItemDetailResponseDTO(
        UUID id,
        String title,
        String description,
        BacklogItemStatus status,
        BacklogItemType type,
        Priority priority,
        Difficulty difficulty,
        Integer estimatedPoints,
        Integer estimatedHours,
        UUID assigneeId,
        UUID parentId,
        UUID sprintId,
        UUID createdById,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt,
        List<BacklogItemDetailResponseDTO> children
) {
}
