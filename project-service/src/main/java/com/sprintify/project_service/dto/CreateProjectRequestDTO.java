package com.sprintify.project_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequestDTO(
        @NotBlank(message = "Project name is required")
        @Size(min = 2, max = 150, message = "Project name must be between 2 and 150 characters")
        String name,

        @Size(max = 4000, message = "Project description must be at most 4000 characters")
        String description,

        String state
) {
}