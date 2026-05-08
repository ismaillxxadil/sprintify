package com.sprintify.project_service.service;

import com.sprintify.project_service.client.GamificationClient;
import com.sprintify.project_service.dto.AwardXpRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncGamificationService {

    private static final int TASK_COMPLETION_XP = 50;

    private final GamificationClient gamificationClient;

    @Async
    public void awardTaskCompletionXp(String userId) {
        try {
            AwardXpRequestDTO request = AwardXpRequestDTO.builder()
                    .userId(userId)
                    .xpAmount(TASK_COMPLETION_XP)
                    .build();

            gamificationClient.awardXp(request);
            log.info("Gamification: awarded {} XP to user {}", TASK_COMPLETION_XP, userId);
        } catch (Exception e) {
            log.error("Gamification award failed for user {}: {}", userId, e.getMessage());
        }
    }
}
