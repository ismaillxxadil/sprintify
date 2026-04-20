package com.sprintify.project_service.dto;

import com.sprintify.project_service.entity.enums.ProjectMemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,
        @NotNull(message = "Role is required")
        ProjectMemberRole role
) {
}
