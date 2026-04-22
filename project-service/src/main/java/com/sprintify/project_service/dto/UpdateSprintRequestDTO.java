package com.sprintify.project_service.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateSprintRequestDTO(
        @Size(max = 150, message = "Title must be max 150 characters")
        String title,

        LocalDate startDate,

        LocalDate endDate,

        @Size(max = 500, message = "Sprint goal must be max 500 characters")
        String sprintGoal
) {
}
