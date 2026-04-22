package com.sprintify.project_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateSprintRequestDTO(
        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must be max 150 characters")
        String title,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        @Size(max = 500, message = "Sprint goal must be max 500 characters")
        String sprintGoal
) {
}
