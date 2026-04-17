package com.sprintify.sprintify.dto;

import java.time.LocalDateTime;

import com.sprintify.sprintify.entity.Role;

public record UserProfileResponseDTO(
        Long id,
        String email,
        Role role,
        LocalDateTime createdAt
) {
}