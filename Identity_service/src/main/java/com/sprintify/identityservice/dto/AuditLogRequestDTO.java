package com.sprintify.identityservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AuditLogRequestDTO {
    private String actorId;
    private String actionType;
    private String entityType;
    private String entityId;
    private Map<String, Object> details;
}