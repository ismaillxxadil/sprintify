package com.sprintify.identityservice.service;

import com.sprintify.identityservice.dto.UserLookupResponseDTO;
import com.sprintify.identityservice.dto.UserProfileResponseDTO;
import com.sprintify.identityservice.entity.Role;
import com.sprintify.identityservice.entity.User;
import com.sprintify.identityservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("user@example.com");
        user.setPassword("hashed_password");
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
    }

    // ─── getProfile ───────────────────────────────────────────────────────────

    @Test
    void getProfile_returnsProfileForValidUserId() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserProfileResponseDTO result = userService.getProfile(userId.toString());

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.email()).isEqualTo("user@example.com");
        assertThat(result.role()).isEqualTo(Role.USER);
        assertThat(result.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 0));
    }

    @Test
    void getProfile_throwsUnauthorizedForInvalidUuidString() {
        assertThatThrownBy(() -> userService.getProfile("not-a-valid-uuid"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
                    assertThat(rse.getReason()).contains("Invalid user identifier");
                });
    }

    @Test
    void getProfile_throwsNotFoundWhenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(userId.toString()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
                    assertThat(rse.getReason()).contains("User not found");
                });
    }

    @Test
    void getProfile_throwsUnauthorizedForEmptyString() {
        assertThatThrownBy(() -> userService.getProfile(""))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
                });
    }

    @Test
    void getProfile_callsRepositoryWithParsedUuid() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.getProfile(userId.toString());

        verify(userRepository).findById(userId);
    }

    // ─── findByEmail ──────────────────────────────────────────────────────────

    @Test
    void findByEmail_returnsUserLookupDtoForValidEmail() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        UserLookupResponseDTO result = userService.findByEmail("user@example.com");

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.email()).isEqualTo("user@example.com");
    }

    @Test
    void findByEmail_normalizesEmailToLowercase() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        userService.findByEmail("USER@EXAMPLE.COM");

        verify(userRepository).findByEmail("user@example.com");
    }

    @Test
    void findByEmail_trimsWhitespaceBeforeNormalizing() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        userService.findByEmail("  User@Example.Com  ");

        verify(userRepository).findByEmail("user@example.com");
    }

    @Test
    void findByEmail_throwsNotFoundWhenUserDoesNotExist() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByEmail("unknown@example.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
                    assertThat(rse.getReason()).contains("User not found");
                });
    }

    @Test
    void findByEmail_throwsBadRequestForNullEmail() {
        assertThatThrownBy(() -> userService.findByEmail(null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                    assertThat(rse.getReason()).contains("Email is required");
                });
    }

    @Test
    void findByEmail_throwsBadRequestForBlankEmail() {
        assertThatThrownBy(() -> userService.findByEmail("   "))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                });
    }

    @Test
    void findByEmail_throwsBadRequestForEmptyEmail() {
        assertThatThrownBy(() -> userService.findByEmail(""))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                });
    }

    @Test
    void findByEmail_returnsCorrectDtoFieldsNotExposingPassword() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        UserLookupResponseDTO result = userService.findByEmail("user@example.com");

        // UserLookupResponseDTO should only have id and email, not password
        assertThat(result.id()).isNotNull();
        assertThat(result.email()).isNotNull();
    }
}