package com.sprintify.project_service.repository;

import com.sprintify.project_service.entity.BacklogItem;
import com.sprintify.project_service.entity.enums.BacklogItemStatus;
import com.sprintify.project_service.entity.enums.BacklogItemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BacklogItemRepository extends JpaRepository<BacklogItem, UUID> {
    
    // Find items in backlog (no sprint assigned)
    //usefull for backlog view and sprint planning
    @Query("SELECT b FROM BacklogItem b WHERE b.project.id = :projectId AND b.sprint IS NULL ORDER BY b.backlogOrder ASC")
    List<BacklogItem> findAllBacklogItems(@Param("projectId") UUID projectId);
    
    // Find items in a sprint scoped to a project
    List<BacklogItem> findAllByProject_IdAndSprint_IdOrderByBacklogOrderAsc(UUID projectId, UUID sprintId);

    // Find items in a sprint
    List<BacklogItem> findAllBySprintIdOrderByBacklogOrderAsc(UUID sprintId);    
    // Find all children of a parent item
    List<BacklogItem> findAllByParentId(UUID parentId);
    
    // Find items by project and type
    Page<BacklogItem> findAllByProject_IdAndType(UUID projectId, BacklogItemType type, Pageable pageable);
    
    // Find items by project and status
    Page<BacklogItem> findAllByProject_IdAndStatus(UUID projectId, BacklogItemStatus status, Pageable pageable);
    
    // Find items by project and assignee
    Page<BacklogItem> findAllByProject_IdAndAssigneeId(UUID projectId, UUID assigneeId, Pageable pageable);
    
    // Check if item exists in project
    boolean existsByIdAndProject_Id(UUID itemId, UUID projectId);
    
    // Find by ID and project (safety check)
    Optional<BacklogItem> findByIdAndProject_Id(UUID itemId, UUID projectId);
}
