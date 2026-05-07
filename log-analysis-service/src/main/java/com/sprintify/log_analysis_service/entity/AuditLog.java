package com.sprintify.log_analysis_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Data
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDateTime timestamp;
    
    private String actorId;     // WHO did it (e.g., User ID)
    private String actionType;  // WHAT they did (e.g., "CREATE_SPRINT")
    private String entityType;  // WHERE it happened (e.g., "PROJECT", "TASK")
    private String entityId;    // Specific target ID

    // This handles the flexible JSON data
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> details; 
}