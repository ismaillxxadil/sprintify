package com.sprintify.identityservice.client;

import com.sprintify.identityservice.dto.AuditLogRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "LOG-ANALYSIS-SERVICE")
public interface LogServiceClient {

    @PostMapping("/api/logs")
    void sendLog(@RequestBody AuditLogRequestDTO logRequest);
}