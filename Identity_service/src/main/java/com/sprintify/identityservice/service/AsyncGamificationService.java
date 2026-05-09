package com.sprintify.identityservice.service;

import com.sprintify.identityservice.client.GamificationClient;
import com.sprintify.identityservice.dto.AwardXpRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncGamificationService {

    private static final int LOGIN_XP = 10;
    private static final int SIGNUP_XP = 50;

    private final GamificationClient gamificationClient;

    @Async
    public void awardLoginXp(UUID userId) {
        try {
            AwardXpRequestDTO request = AwardXpRequestDTO.builder()
                    .userId(userId.toString())
                    .xpAmount(LOGIN_XP)
                    .build();

            gamificationClient.awardXp(request);
            log.info("Gamification: awarded {} XP to user {} on login", LOGIN_XP, userId);
        } catch (Exception e) {
            log.error("Gamification award failed for user {} on login: {}", userId, e.getMessage());
        }
    }

    @Async
    public void awardSignupXp(UUID userId) {
        try {
            AwardXpRequestDTO request = AwardXpRequestDTO.builder()
                    .userId(userId.toString())
                    .xpAmount(SIGNUP_XP)
                    .build();

            gamificationClient.awardXp(request);
            log.info("Gamification: awarded {} XP to user {} on signup", SIGNUP_XP, userId);
        } catch (Exception e) {
            log.error("Gamification award failed for user {} on signup: {}", userId, e.getMessage());
        }
    }

    @Async
    public void checkStreak(UUID userId) {
        try {
            gamificationClient.checkInStreak(userId.toString());
            log.info("Gamification: checked streak for user {}", userId);
        } catch (Exception e) {
            log.error("Gamification streak check failed for user {}: {}", userId, e.getMessage());
        }
    }
}
