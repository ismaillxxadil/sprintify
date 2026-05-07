package com.sprintify.log_analysis_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sprintify.log_analysis_service.entity.AuditLog;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    
    // Fetch a specific user's activity timeline, newest first
    List<AuditLog> findByActorIdOrderByTimestampDesc(String actorId);

    // Fetch the history of a specific entity (like a Project or a Sprint), newest first
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, String entityId);
}