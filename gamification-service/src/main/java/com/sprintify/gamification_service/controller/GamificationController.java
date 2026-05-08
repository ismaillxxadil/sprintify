package main.java.com.sprintify.gamification_service.controller;

import main.java.com.sprintify.gamification_service.dto.AwardXpRequestDTO;
import main.java.com.sprintify.gamification_service.Entity.GamificationProfile;
import main.java.com.sprintify.gamification_service.service.GamificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;

    @GetMapping("/profile/{userId}")
    public ResponseEntity<GamificationProfile> getProfile(@PathVariable String userId) {
        return ResponseEntity.ok(gamificationService.getProfile(userId));
    }

    @PostMapping("/award")
    public ResponseEntity<String> awardXp(@Valid @RequestBody AwardXpRequestDTO request) {
        gamificationService.awardXp(request.getUserId(), request.getXpAmount());
        return ResponseEntity.ok("XP awarded successfully!");
    }

    @PostMapping("/streak/check-in/{userId}")
    public ResponseEntity<String> checkInStreak(@PathVariable String userId) {
        gamificationService.updateStreak(userId);
        return ResponseEntity.ok("Daily streak updated!");
    }
}
