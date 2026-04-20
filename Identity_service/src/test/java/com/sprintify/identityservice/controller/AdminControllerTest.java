package com.sprintify.identityservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprintify.identityservice.dto.AdminUserResponseDTO;
import com.sprintify.identityservice.entity.Role;
import com.sprintify.identityservice.security.JwtAuthenticationFilter;
import com.sprintify.identityservice.security.JwtService;
import com.sprintify.identityservice.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminService adminService;

    // Required to satisfy SecurityConfig dependencies
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    // ─── GET /api/v1/admin/users ───────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_returns200WithUserListForAdminRole() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<AdminUserResponseDTO> users = List.of(
                new AdminUserResponseDTO(id1, "alice@example.com", Role.USER, LocalDateTime.of(2024, 1, 1, 0, 0), null),
                new AdminUserResponseDTO(id2, "bob@example.com", Role.ADMIN, LocalDateTime.of(2024, 2, 1, 0, 0), null)
        );
        when(adminService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(id1.toString()))
                .andExpect(jsonPath("$[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$[0].role").value("USER"))
                .andExpect(jsonPath("$[1].id").value(id2.toString()))
                .andExpect(jsonPath("$[1].email").value("bob@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_returns200WithEmptyListWhenNoUsers() throws Exception {
        when(adminService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllUsers_returns401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_returns403WhenAuthenticatedAsNonAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_responseDoesNotContainPasswordField() throws Exception {
        UUID id = UUID.randomUUID();
        when(adminService.getAllUsers()).thenReturn(List.of(
                new AdminUserResponseDTO(id, "user@example.com", Role.USER, LocalDateTime.now(), null)
        ));

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    // ─── DELETE /api/v1/admin/users/{id} ──────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_returns200WithSuccessMessageForAdminRole() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(adminService).deleteUser(userId);

        mockMvc.perform(delete("/api/v1/admin/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(content().string("Success: User has been deleted permanently."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_returns404WhenUserNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId))
                .when(adminService).deleteUser(userId);

        mockMvc.perform(delete("/api/v1/admin/users/" + userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_returns401WhenNotAuthenticated() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/admin/users/" + userId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUser_returns403WhenAuthenticatedAsNonAdmin() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/admin/users/" + userId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_callsServiceWithCorrectUuid() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(adminService).deleteUser(userId);

        mockMvc.perform(delete("/api/v1/admin/users/" + userId))
                .andExpect(status().isOk());

        verify(adminService).deleteUser(userId);
    }

    // ─── POST /api/v1/admin/users/{id}/ban ────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void banUser_returns200WithSuccessMessageForAdminRole() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(adminService).banUser(userId, 7);

        mockMvc.perform(post("/api/v1/admin/users/" + userId + "/ban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("days", 7))))
                .andExpect(status().isOk())
                .andExpect(content().string("Success: User has been banned for 7 day(s)."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void banUser_returns404WhenUserNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId))
                .when(adminService).banUser(userId, 3);

        mockMvc.perform(post("/api/v1/admin/users/" + userId + "/ban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("days", 3))))
                .andExpect(status().isNotFound());
    }

    @Test
    void banUser_returns401WhenNotAuthenticated() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/users/" + userId + "/ban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("days", 1))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void banUser_returns403WhenAuthenticatedAsNonAdmin() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/users/" + userId + "/ban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("days", 1))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void banUser_returns400WhenDaysIsZero() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/users/" + userId + "/ban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("days", 0))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void banUser_returns400WhenDaysIsNegative() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/users/" + userId + "/ban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("days", -1))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void banUser_callsServiceWithCorrectUuidAndDays() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(adminService).banUser(userId, 30);

        mockMvc.perform(post("/api/v1/admin/users/" + userId + "/ban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("days", 30))))
                .andExpect(status().isOk());

        verify(adminService).banUser(userId, 30);
    }
}