package com.sprintify.project_service.dto;

import com.sprintify.project_service.entity.enums.BacklogItemStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateBacklogItemStatusRequestDTO(
        @NotNull(message = "Status is required")
        BacklogItemStatus status,

        String comment
) {
}
