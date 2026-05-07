package com.sprintify.project_service.client;

import com.sprintify.project_service.dto.AuditLogRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "LOG-ANALYSIS-SERVICE")
public interface LogServiceClient {

    @PostMapping("/api/logs")
    void sendLog(@RequestBody AuditLogRequestDTO logRequest);
}