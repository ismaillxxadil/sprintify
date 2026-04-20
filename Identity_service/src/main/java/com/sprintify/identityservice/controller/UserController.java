package com.sprintify.identityservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sprintify.identityservice.dto.UserLookupResponseDTO;
import com.sprintify.identityservice.dto.UserProfileResponseDTO;
import com.sprintify.identityservice.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponseDTO> getProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getProfile(authentication.getName()));
    }

    @GetMapping("/resolve")
    public ResponseEntity<UserLookupResponseDTO> resolveByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.findByEmail(email));
    }
}