package com.sprintify.identityservice.dto;

import com.sprintify.identityservice.entity.Role;

public record AuthResponseDTO(
        String token,
        String email,
        Role role
) {
}
