package com.sprintify.identityservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sprintify.identityservice.entity.Role;

public record UserProfileResponseDTO(
        UUID id,
        String email,
        Role role,
        LocalDateTime createdAt
) {
}