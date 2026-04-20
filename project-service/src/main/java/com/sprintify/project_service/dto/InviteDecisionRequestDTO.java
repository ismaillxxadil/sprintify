package com.sprintify.project_service.dto;

import jakarta.validation.constraints.NotNull;

public record InviteDecisionRequestDTO(
        @NotNull(message = "Decision is required")
        InviteDecision decision
) {
}
