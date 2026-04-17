package com.sprintify.sprintify.dto;

import com.sprintify.sprintify.entity.Role;

public record AuthResponseDTO(
        String token,
        String email,
        Role role
) {
}
