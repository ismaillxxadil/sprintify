package com.sprintify.project_service.dto;

import com.sprintify.project_service.entity.enums.Difficulty;
import com.sprintify.project_service.entity.enums.Priority;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateBacklogItemRequestDTO(
        @Size(max = 200, message = "Title must be max 200 characters")
        String title,

        @Size(max = 4000, message = "Description must be max 4000 characters")
        String description,

        Priority priority,

        Difficulty difficulty,

        @PositiveOrZero(message = "Estimated points must be >= 0")
        Integer estimatedPoints,

        @PositiveOrZero(message = "Estimated hours must be >= 0")
        Integer estimatedHours
) {
}
