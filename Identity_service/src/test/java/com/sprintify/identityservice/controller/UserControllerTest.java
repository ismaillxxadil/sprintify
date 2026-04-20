package com.sprintify.identityservice.controller;

import com.sprintify.identityservice.dto.UserLookupResponseDTO;
import com.sprintify.identityservice.dto.UserProfileResponseDTO;
import com.sprintify.identityservice.entity.Role;
import com.sprintify.identityservice.security.JwtAuthenticationFilter;
import com.sprintify.identityservice.security.JwtService;
import com.sprintify.identityservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    // Required to satisfy the SecurityConfig bean dependency
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    // ─── GET /api/v1/users/profile ────────────────────────────────────────────

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void getProfile_returns200WithProfileDataForAuthenticatedUser() throws Exception {
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UserProfileResponseDTO dto = new UserProfileResponseDTO(
                userId,
                "user@example.com",
                Role.USER,
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
        when(userService.getProfile("550e8400-e29b-41d4-a716-446655440000")).thenReturn(dto);

        mockMvc.perform(get("/api/v1/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void getProfile_returns401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/users/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void getProfile_returns404WhenUserNotFound() throws Exception {
        when(userService.getProfile(anyString()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(get("/api/v1/users/profile"))
                .andExpect(status().isNotFound());
    }

    // ─── GET /api/v1/users/resolve ────────────────────────────────────────────

    @Test
    void resolveByEmail_returns200WithUserLookupDtoForValidEmail() throws Exception {
        UUID userId = UUID.randomUUID();
        UserLookupResponseDTO dto = new UserLookupResponseDTO(userId, "user@example.com");
        when(userService.findByEmail("user@example.com")).thenReturn(dto);

        mockMvc.perform(get("/api/v1/users/resolve")
                        .param("email", "user@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void resolveByEmail_isPublicEndpointNoAuthRequired() throws Exception {
        UUID userId = UUID.randomUUID();
        UserLookupResponseDTO dto = new UserLookupResponseDTO(userId, "public@example.com");
        when(userService.findByEmail("public@example.com")).thenReturn(dto);

        // No authentication set up - should still work because /resolve is permitAll
        mockMvc.perform(get("/api/v1/users/resolve")
                        .param("email", "public@example.com"))
                .andExpect(status().isOk());
    }

    @Test
    void resolveByEmail_returns404WhenUserNotFound() throws Exception {
        when(userService.findByEmail("missing@example.com"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(get("/api/v1/users/resolve")
                        .param("email", "missing@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void resolveByEmail_returns400WhenEmailIsBlank() throws Exception {
        when(userService.findByEmail(""))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required"));

        mockMvc.perform(get("/api/v1/users/resolve")
                        .param("email", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resolveByEmail_returns400WhenEmailParamMissing() throws Exception {
        // No "email" param - Spring should return 400 for missing required request param
        mockMvc.perform(get("/api/v1/users/resolve"))
                .andExpect(status().isBadRequest());
    }
}