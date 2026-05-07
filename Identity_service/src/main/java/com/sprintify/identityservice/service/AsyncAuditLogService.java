package com.sprintify.identityservice.service;

import com.sprintify.identityservice.client.LogServiceClient;
import com.sprintify.identityservice.dto.AuditLogRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncAuditLogService {

    private final LogServiceClient logServiceClient;

    @Async
    public void sendLogAsynchronously(AuditLogRequestDTO logRequest) {
        try {
            logServiceClient.sendLog(logRequest);
            log.info("Audit log sent successfully in the background.");
        } catch (Exception e) {
            log.error("Failed to send audit log to Log Service: {}", e.getMessage());
        }
    }
}