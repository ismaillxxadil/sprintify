package com.sprintify.identityservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.sprintify.identityservice.dto.AdminUserResponseDTO; 
import com.sprintify.identityservice.entity.User;
import com.sprintify.identityservice.repository.UserRepository;

@Service
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<AdminUserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toAdminUserResponse)
                .toList();
    }

    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id);
        }

        userRepository.deleteById(id);
    }

    public void banUser(UUID id, Integer days) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id));

        LocalDateTime bannedUntil = LocalDateTime.now().plusDays(days);
        user.setBannedUntil(bannedUntil);
        userRepository.save(user);
    }

    private AdminUserResponseDTO toAdminUserResponse(User user) {
        return new AdminUserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getBannedUntil()
        );
    }
}