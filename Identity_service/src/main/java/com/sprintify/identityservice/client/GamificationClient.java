package com.sprintify.identityservice.client;

import com.sprintify.identityservice.dto.AwardXpRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "GAMIFICATION-SERVICE")
public interface GamificationClient {

    @PostMapping("/api/gamification/award")
    void awardXp(@RequestBody AwardXpRequestDTO request);

    @PostMapping("/api/gamification/streak/check-in/{userId}")
    void checkInStreak(@PathVariable String userId);
}
