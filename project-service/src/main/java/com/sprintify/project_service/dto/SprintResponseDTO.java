package com.sprintify.project_service.dto;

import com.sprintify.project_service.entity.enums.SprintStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SprintResponseDTO(
        UUID id,
        String title,
        String sprintGoal,
        SprintStatus status,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime startedAt,
        LocalDateTime closedAt,
        Integer totalPoints,
        Integer completedPoints,
        Integer remainingPoints,
        Float velocity,
        List<BacklogItemResponseDTO> backlogItems
) {
}
