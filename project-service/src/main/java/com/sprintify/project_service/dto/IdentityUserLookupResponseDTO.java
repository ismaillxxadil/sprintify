package com.sprintify.project_service.dto;

import java.util.UUID;

public record IdentityUserLookupResponseDTO(
        UUID id,
        String email
) {
}
