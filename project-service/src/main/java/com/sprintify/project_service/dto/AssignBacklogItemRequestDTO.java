package com.sprintify.project_service.dto;

import java.util.UUID;

public record AssignBacklogItemRequestDTO(
        UUID assigneeId
) {
}
