package com.sprintify.log_analysis_service.Controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sprintify.log_analysis_service.Repository.AuditLogRepository;
import com.sprintify.log_analysis_service.entity.AuditLog;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    @Autowired
    private AuditLogRepository repository;

    // 1. WRITE ENDPOINT: Used by your Async AOP clients in other services
    @PostMapping
    public ResponseEntity<String> saveLog(@RequestBody AuditLog log) {
        log.setTimestamp(LocalDateTime.now()); 
        repository.save(log);
        return ResponseEntity.ok("Log saved successfully");
    }

    // 2. READ ENDPOINT: Get timeline for a specific user
    // Example: GET /api/logs/user/123
    @GetMapping("/user/{actorId}")
    public ResponseEntity<List<AuditLog>> getUserTimeline(@PathVariable String actorId) {
        List<AuditLog> logs = repository.findByActorIdOrderByTimestampDesc(actorId);
        return ResponseEntity.ok(logs);
    }

    // 3. READ ENDPOINT: Get history for a specific project, sprint, or task
    // Example: GET /api/logs/entity/PROJECT/456
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLog>> getEntityHistory(
            @PathVariable String entityType, 
            @PathVariable String entityId) {
        List<AuditLog> logs = repository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
        return ResponseEntity.ok(logs);
    }

    //get all logs
    @GetMapping("/all")
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        
        List<AuditLog> logs = repository.findAll();
        logs.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())); // Sort by timestamp descending
        return ResponseEntity.ok(logs);
    }
}