package com.sprintify.identityservice.service;

import com.sprintify.identityservice.dto.AdminUserResponseDTO;
import com.sprintify.identityservice.entity.Role;
import com.sprintify.identityservice.entity.User;
import com.sprintify.identityservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminService adminService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("admin@example.com");
        user.setPassword("hashed_password");
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
    }

    // ─── getAllUsers ───────────────────────────────────────────────────────────

    @Test
    void getAllUsers_returnsEmptyListWhenNoUsersExist() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<AdminUserResponseDTO> result = adminService.getAllUsers();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllUsers_returnsMappedDtosForAllUsers() {
        User user2 = new User();
        UUID user2Id = UUID.randomUUID();
        user2.setId(user2Id);
        user2.setEmail("user2@example.com");
        user2.setRole(Role.ADMIN);
        user2.setCreatedAt(LocalDateTime.now());

        when(userRepository.findAll()).thenReturn(List.of(user, user2));

        List<AdminUserResponseDTO> result = adminService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(userId);
        assertThat(result.get(0).email()).isEqualTo("admin@example.com");
        assertThat(result.get(0).role()).isEqualTo(Role.USER);
        assertThat(result.get(1).id()).isEqualTo(user2Id);
        assertThat(result.get(1).email()).isEqualTo("user2@example.com");
        assertThat(result.get(1).role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void getAllUsers_includesBannedUntilField() {
        LocalDateTime bannedUntil = LocalDateTime.now().plusDays(7);
        user.setBannedUntil(bannedUntil);
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<AdminUserResponseDTO> result = adminService.getAllUsers();

        assertThat(result.get(0).bannedUntil()).isEqualTo(bannedUntil);
    }

    @Test
    void getAllUsers_doesNotExposePasswordField() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<AdminUserResponseDTO> result = adminService.getAllUsers();

        // AdminUserResponseDTO should not have a password field
        assertThat(result.get(0)).isInstanceOf(AdminUserResponseDTO.class);
        // record components: id, email, role, createdAt, bannedUntil - no password
        assertThat(result.get(0).id()).isNotNull();
        assertThat(result.get(0).email()).isNotNull();
    }

    // ─── deleteUser ───────────────────────────────────────────────────────────

    @Test
    void deleteUser_deletesUserSuccessfully() {
        when(userRepository.existsById(userId)).thenReturn(true);

        adminService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_throwsNotFoundWhenUserDoesNotExist() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> adminService.deleteUser(userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
                    assertThat(rse.getReason()).contains("User not found with ID: " + userId);
                });
    }

    @Test
    void deleteUser_doesNotCallDeleteWhenUserDoesNotExist() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> adminService.deleteUser(userId))
                .isInstanceOf(ResponseStatusException.class);

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void deleteUser_checksExistenceWithCorrectUuid() {
        when(userRepository.existsById(userId)).thenReturn(true);

        adminService.deleteUser(userId);

        verify(userRepository).existsById(userId);
    }

    // ─── banUser ──────────────────────────────────────────────────────────────

    @Test
    void banUser_setsBannedUntilDateCorrectly() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        LocalDateTime beforeBan = LocalDateTime.now().plusDays(7).minusSeconds(1);
        adminService.banUser(userId, 7);
        LocalDateTime afterBan = LocalDateTime.now().plusDays(7).plusSeconds(1);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();

        assertThat(savedUser.getBannedUntil()).isAfter(beforeBan).isBefore(afterBan);
    }

    @Test
    void banUser_throwsNotFoundWhenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.banUser(userId, 3))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
                    assertThat(rse.getReason()).contains("User not found with ID: " + userId);
                });
    }

    @Test
    void banUser_savesUserAfterSettingBannedUntil() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        adminService.banUser(userId, 1);

        verify(userRepository).save(user);
    }

    @Test
    void banUser_oneDayBanSetsDateInFuture() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        adminService.banUser(userId, 1);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getBannedUntil()).isAfter(LocalDateTime.now());
    }

    @Test
    void banUser_largeBanPeriodSetsFarFutureDate() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        adminService.banUser(userId, 365);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getBannedUntil())
                .isAfter(LocalDateTime.now().plusDays(360));
    }
}