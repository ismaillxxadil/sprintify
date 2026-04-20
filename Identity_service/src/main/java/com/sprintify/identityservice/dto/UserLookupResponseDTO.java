package com.sprintify.identityservice.dto;

import java.util.UUID;

public record UserLookupResponseDTO(
        UUID id,
        String email
) {
}
