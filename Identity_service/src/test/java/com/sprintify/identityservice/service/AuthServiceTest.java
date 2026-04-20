package com.sprintify.identityservice.service;

import com.sprintify.identityservice.dto.AuthResponseDTO;
import com.sprintify.identityservice.dto.LoginRequestDTO;
import com.sprintify.identityservice.dto.SignUpRequestDTO;
import com.sprintify.identityservice.entity.Role;
import com.sprintify.identityservice.entity.User;
import com.sprintify.identityservice.repository.UserRepository;
import com.sprintify.identityservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private UUID userId;
    private User savedUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        savedUser = new User();
        savedUser.setId(userId);
        savedUser.setEmail("user@example.com");
        savedUser.setPassword("$2a$10$hashedPassword");
        savedUser.setRole(Role.USER);
    }

    // ─── signUp ───────────────────────────────────────────────────────────────

    @Test
    void signUp_returnsAuthResponseDtoOnSuccess() {
        SignUpRequestDTO request = new SignUpRequestDTO("user@example.com", "StrongPass123");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("StrongPass123")).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(eq(userId), eq("user@example.com"), eq(Role.USER)))
                .thenReturn("jwt-token");

        AuthResponseDTO result = authService.signUp(request);

        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.email()).isEqualTo("user@example.com");
        assertThat(result.role()).isEqualTo(Role.USER);
    }

    @Test
    void signUp_normalizesEmailToLowercaseAndTrimmed() {
        SignUpRequestDTO request = new SignUpRequestDTO("  USER@EXAMPLE.COM  ", "StrongPass123");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(), anyString(), any())).thenReturn("token");

        authService.signUp(request);

        verify(userRepository).existsByEmail("user@example.com");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("user@example.com");
    }

    @Test
    void signUp_throwsBadRequestWhenEmailAlreadyTaken() {
        SignUpRequestDTO request = new SignUpRequestDTO("existing@example.com", "StrongPass123");
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                    assertThat(rse.getReason()).contains("Email is already taken");
                });
    }

    @Test
    void signUp_doesNotSaveUserWhenEmailIsTaken() {
        SignUpRequestDTO request = new SignUpRequestDTO("taken@example.com", "StrongPass123");
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(ResponseStatusException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void signUp_encodesPasswordBeforeSaving() {
        SignUpRequestDTO request = new SignUpRequestDTO("user@example.com", "PlainPassword123");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("PlainPassword123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(), anyString(), any())).thenReturn("token");

        authService.signUp(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    void signUp_generateTokenWithSavedUserId() {
        SignUpRequestDTO request = new SignUpRequestDTO("user@example.com", "StrongPass123");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(userId, "user@example.com", Role.USER)).thenReturn("token");

        authService.signUp(request);

        verify(jwtService).generateToken(userId, "user@example.com", Role.USER);
    }

    // ─── login ────────────────────────────────────────────────────────────────

    @Test
    void login_returnsAuthResponseDtoOnSuccess() {
        LoginRequestDTO request = new LoginRequestDTO("user@example.com", "PlainPassword");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("PlainPassword", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(userId, "user@example.com", Role.USER)).thenReturn("jwt-token");

        AuthResponseDTO result = authService.login(request);

        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.email()).isEqualTo("user@example.com");
        assertThat(result.role()).isEqualTo(Role.USER);
    }

    @Test
    void login_normalizesEmailBeforeLookup() {
        LoginRequestDTO request = new LoginRequestDTO("  USER@EXAMPLE.COM  ", "pass");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(any(), anyString(), any())).thenReturn("token");

        authService.login(request);

        verify(userRepository).findByEmail("user@example.com");
    }

    @Test
    void login_throwsUnauthorizedWhenUserNotFound() {
        LoginRequestDTO request = new LoginRequestDTO("unknown@example.com", "password");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
                    assertThat(rse.getReason()).contains("Invalid email or password");
                });
    }

    @Test
    void login_throwsUnauthorizedWhenPasswordDoesNotMatch() {
        LoginRequestDTO request = new LoginRequestDTO("user@example.com", "WrongPassword");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("WrongPassword", "$2a$10$hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
                    assertThat(rse.getReason()).contains("Invalid email or password");
                });
    }

    @Test
    void login_doesNotGenerateTokenOnWrongPassword() {
        LoginRequestDTO request = new LoginRequestDTO("user@example.com", "WrongPassword");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResponseStatusException.class);

        verify(jwtService, never()).generateToken(any(), anyString(), any());
    }

    @Test
    void login_generatesTokenWithUserIdEmailAndRole() {
        LoginRequestDTO request = new LoginRequestDTO("user@example.com", "CorrectPass");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("CorrectPass", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(userId, "user@example.com", Role.USER)).thenReturn("token");

        authService.login(request);

        verify(jwtService).generateToken(userId, "user@example.com", Role.USER);
    }

    @Test
    void login_invalidCredentialsMessageDoesNotRevealWhichFieldFailed() {
        // Same error message for both "user not found" and "wrong password" - prevents enumeration
        LoginRequestDTO requestWithBadEmail = new LoginRequestDTO("nouser@example.com", "pass");
        LoginRequestDTO requestWithBadPass = new LoginRequestDTO("user@example.com", "wrongpass");

        when(userRepository.findByEmail("nouser@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("wrongpass", "$2a$10$hashedPassword")).thenReturn(false);

        String messageForBadEmail = null;
        String messageForBadPass = null;
        try {
            authService.login(requestWithBadEmail);
        } catch (ResponseStatusException ex) {
            messageForBadEmail = ex.getReason();
        }
        try {
            authService.login(requestWithBadPass);
        } catch (ResponseStatusException ex) {
            messageForBadPass = ex.getReason();
        }

        assertThat(messageForBadEmail).isEqualTo(messageForBadPass);
        assertThat(messageForBadEmail).isEqualTo("Invalid email or password");
    }
}