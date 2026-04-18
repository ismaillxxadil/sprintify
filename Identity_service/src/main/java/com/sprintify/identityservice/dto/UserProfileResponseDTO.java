package com.sprintify.identityservice.dto;

import java.time.LocalDateTime;

import com.sprintify.identityservice.entity.Role;

public record UserProfileResponseDTO(
        Long id,
        String email,
        Role role,
        LocalDateTime createdAt
) {
}