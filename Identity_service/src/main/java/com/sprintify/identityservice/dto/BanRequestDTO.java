package com.sprintify.identityservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BanRequestDTO(
        @NotNull(message = "Days is required")
        @Min(value = 1, message = "Days must be at least 1")
        Integer days
) {
}
