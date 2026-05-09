package com.sprintify.gamification_service.service;

import com.sprintify.gamification_service.Entity.GamificationProfile;
import com.sprintify.gamification_service.repository.GamificationProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GamificationService {

    private final GamificationProfileRepository repository;

    public GamificationProfile getProfile(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("userId must not be blank");
        }

        return repository.findByUserId(userId)
                .orElseGet(() -> {
                    GamificationProfile newProfile = new GamificationProfile(userId);
                    return repository.save(newProfile);
                });
    }

    public List<GamificationProfile> getAllProfiles() {
        return repository.findAll();
    }

    @Transactional
    public GamificationProfile awardXp(String userId, int xpAmount) {
        if (xpAmount <= 0) {
            throw new IllegalArgumentException("xpAmount must be greater than 0");
        }

        GamificationProfile profile = getProfile(userId);
        profile.setTotalXp(profile.getTotalXp() + xpAmount);

        int calculatedLevel = (profile.getTotalXp() / 500) + 1;
        if (calculatedLevel > profile.getCurrentLevel()) {
            profile.setCurrentLevel(calculatedLevel);
        }

        return repository.save(profile);
    }

    @Transactional
    public GamificationProfile updateStreak(String userId) {
        GamificationProfile profile = getProfile(userId);
        LocalDate today = LocalDate.now();
        LocalDate lastAction = profile.getLastActionDate();

        if (lastAction == null) {
            profile.setCurrentStreak(1);
        } else if (lastAction.equals(today.minusDays(1))) {
            profile.setCurrentStreak(profile.getCurrentStreak() + 1);
        } else if (lastAction.isBefore(today.minusDays(1))) {
            profile.setCurrentStreak(1);
        }

        profile.setLastActionDate(today);
        return repository.save(profile);
    }
}
