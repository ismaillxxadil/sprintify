package com.sprintify.identityservice.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setPassword("hashed_password");
        user.setRole(Role.USER);
    }

    // ─── isBanned ─────────────────────────────────────────────────────────────

    @Test
    void isBanned_returnsFalseWhenBannedUntilIsNull() {
        user.setBannedUntil(null);

        assertThat(user.isBanned()).isFalse();
    }

    @Test
    void isBanned_returnsTrueWhenBannedUntilIsInFuture() {
        user.setBannedUntil(LocalDateTime.now().plusDays(1));

        assertThat(user.isBanned()).isTrue();
    }

    @Test
    void isBanned_returnsFalseWhenBannedUntilIsInPast() {
        user.setBannedUntil(LocalDateTime.now().minusSeconds(1));

        assertThat(user.isBanned()).isFalse();
    }

    @Test
    void isBanned_returnsFalseWhenBannedUntilIsExactlyNow() {
        // bannedUntil that is exactly now (or just before now) should return false
        user.setBannedUntil(LocalDateTime.now().minusNanos(1));

        assertThat(user.isBanned()).isFalse();
    }

    @Test
    void isBanned_returnsTrueWhenBannedForManyDays() {
        user.setBannedUntil(LocalDateTime.now().plusDays(365));

        assertThat(user.isBanned()).isTrue();
    }

    // ─── Default role ─────────────────────────────────────────────────────────

    @Test
    void defaultRole_isUser() {
        User freshUser = new User();

        assertThat(freshUser.getRole()).isEqualTo(Role.USER);
    }

    // ─── UUID id type ─────────────────────────────────────────────────────────

    @Test
    void setAndGetId_worksWithUuid() {
        UUID id = UUID.randomUUID();
        user.setId(id);

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getId()).isInstanceOf(UUID.class);
    }

    @Test
    void getId_returnsNullForNewUserBeforePersist() {
        User newUser = new User();

        assertThat(newUser.getId()).isNull();
    }

    // ─── Getters/Setters ──────────────────────────────────────────────────────

    @Test
    void setAndGetEmail_works() {
        user.setEmail("test@test.com");

        assertThat(user.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void setAndGetRole_works() {
        user.setRole(Role.ADMIN);

        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void setAndGetBannedUntil_works() {
        LocalDateTime bannedUntil = LocalDateTime.of(2025, 12, 31, 23, 59);
        user.setBannedUntil(bannedUntil);

        assertThat(user.getBannedUntil()).isEqualTo(bannedUntil);
    }
}