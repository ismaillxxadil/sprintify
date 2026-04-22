package com.sprintify.project_service.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record AddItemsToSprintRequestDTO(
        @NotEmpty(message = "Item IDs list cannot be empty")
        List<@NotNull(message = "Item ID is required") UUID> itemIds
) {
}
