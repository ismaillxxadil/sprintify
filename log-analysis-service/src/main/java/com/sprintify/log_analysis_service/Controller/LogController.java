package com.sprintify.log_analysis_service.Controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sprintify.log_analysis_service.Repository.AuditLogRepository;
import com.sprintify.log_analysis_service.entity.AuditLog;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    @Autowired
    private AuditLogRepository repository;

    @PostMapping("/save")
    public String save(@RequestBody AuditLog log) {
        log.setTimestamp(LocalDateTime.now()); 
        repository.save(log);
        return "Log Analysis: Data received and saved successfully!";
    }
}