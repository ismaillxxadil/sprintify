package com.sprintify.identityservice.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sprintify.identityservice.dto.AdminUserResponseDTO;
import com.sprintify.identityservice.dto.BanRequestDTO; 
import com.sprintify.identityservice.service.AdminService;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Fetch all registered users for the Admin Dashboard
     */
    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /**
     * Delete a user by their unique UUID
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok("Success: User has been deleted permanently.");
    }

    /**
     * Ban a user for a specified number of days
     */
    @PostMapping("/users/{id}/ban")
    public ResponseEntity<String> banUser(@PathVariable Long id, @Valid @RequestBody BanRequestDTO request) {
        adminService.banUser(id, request.days());
        return ResponseEntity.ok("Success: User has been banned for " + request.days() + " day(s).");
    }
}