package com.sprintify.project_service.repository;

import com.sprintify.project_service.entity.Sprint;
import com.sprintify.project_service.entity.enums.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SprintRepository extends JpaRepository<Sprint, UUID> {
    
    // Find all sprints in a project, ordered by start date
    List<Sprint> findAllByProject_IdOrderByStartDateDesc(UUID projectId);
    
    // Find all sprints in a project with specific status
    List<Sprint> findAllByProject_IdAndStatusOrderByStartDateDesc(UUID projectId, SprintStatus status);
    
    // Check if sprint exists in project
    boolean existsByIdAndProject_Id(UUID sprintId, UUID projectId);
    
    // Find by ID and project (safety check)
    Optional<Sprint> findByIdAndProject_Id(UUID sprintId, UUID projectId);
    
    // Find active sprint in project
    @Query("SELECT s FROM Sprint s WHERE s.project.id = :projectId AND s.status = 'ACTIVE'")
    Optional<Sprint> findActiveSprint(@Param("projectId") UUID projectId);
}
