package com.sprintify.project_service.client;

import com.sprintify.project_service.dto.AwardXpRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "GAMIFICATION-SERVICE")
public interface GamificationClient {

    @PostMapping("/api/gamification/award")
    void awardXp(@RequestBody AwardXpRequestDTO request);
}
