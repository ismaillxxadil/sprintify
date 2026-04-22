package com.sprintify.project_service.service;

import com.sprintify.project_service.dto.*;
import com.sprintify.project_service.entity.BacklogItem;
import com.sprintify.project_service.entity.Project;
import com.sprintify.project_service.entity.Sprint;
import com.sprintify.project_service.entity.enums.*;
import com.sprintify.project_service.entity.ProjectMember;
import com.sprintify.project_service.repository.BacklogItemRepository;
import com.sprintify.project_service.repository.ProjectMemberRepository;
import com.sprintify.project_service.repository.ProjectRepository;
import com.sprintify.project_service.repository.SprintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SprintService {

    private final SprintRepository sprintRepository;
    private final BacklogItemRepository backlogItemRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;

    // ============ AUTHORIZATION HELPERS ============

    private ProjectMember getMember(UUID userId, UUID projectId) {
        return projectMemberRepository.findByProject_IdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not a member of this project"));
    }

    private void validatePOorSM(UUID userId, UUID projectId) {
        ProjectMember member = getMember(userId, projectId);
        if (member.getRole() != ProjectMemberRole.PO && member.getRole() != ProjectMemberRole.SM) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only PO or SM can perform this action");
        }
    }

    // ============ CREATE ============

    @Transactional
    public SprintResponseDTO createSprint(UUID projectId, UUID userId, CreateSprintRequestDTO request) {
        validatePOorSM(userId, projectId);

        // Verify project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        // Validate dates
        if (!request.endDate().isAfter(request.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }

        Sprint sprint = Sprint.builder()
                .project(project)
                .title(request.title().trim())
                .sprintGoal(request.sprintGoal() != null ? request.sprintGoal().trim() : null)
                .status(SprintStatus.PLANNING)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        Sprint saved = sprintRepository.save(sprint);
        return toResponse(saved);
    }

    // ============ READ ============

    @Transactional(readOnly = true)
    public SprintResponseDTO getSprint(UUID projectId, UUID sprintId) {
        Sprint sprint = sprintRepository.findByIdAndProject_Id(sprintId, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sprint not found"));
        return toResponse(sprint);
    }

    @Transactional(readOnly = true)
    public List<SprintResponseDTO> listSprints(UUID projectId) {
        return sprintRepository.findAllByProject_IdOrderByStartDateDesc(projectId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ============ UPDATE ============

    @Transactional
    public SprintResponseDTO addItemsToSprint(UUID projectId, UUID userId, UUID sprintId, AddItemsToSprintRequestDTO request) {
        validatePOorSM(userId, projectId);

        Sprint sprint = sprintRepository.findByIdAndProject_Id(sprintId, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sprint not found"));

        if (sprint.getStatus() != SprintStatus.PLANNING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Can only add items to sprint in PLANNING state");
        }

        // Verify all items exist in this project and add them
        for (UUID itemId : request.itemIds()) {
            BacklogItem item = backlogItemRepository.findByIdAndProject_Id(itemId, projectId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item " + itemId + " not found"));

            // Verify item is not already in another sprint
            if (item.getSprint() != null && !item.getSprint().getId().equals(sprintId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Item " + itemId + " is already in another sprint");
            }

            item.setSprint(sprint);
            backlogItemRepository.save(item);
        }

        sprint = sprintRepository.findById(sprintId).orElseThrow();
        return toResponse(sprint);
    }

    @Transactional
    public SprintResponseDTO removeItemFromSprint(UUID projectId, UUID userId, UUID sprintId, UUID itemId) {
        validatePOorSM(userId, projectId);

        Sprint sprint = sprintRepository.findByIdAndProject_Id(sprintId, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sprint not found"));

        if (sprint.getStatus() != SprintStatus.PLANNING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Can only remove items from sprint in PLANNING state");
        }

        BacklogItem item = backlogItemRepository.findByIdAndProject_Id(itemId, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        // Check if item is in this sprint
        if (item.getSprint() == null || !item.getSprint().getId().equals(sprintId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item is not in this sprint");
        }

        item.setSprint(null);
        backlogItemRepository.save(item);

        sprint = sprintRepository.findById(sprintId).orElseThrow();
        return toResponse(sprint);
    }

    @Transactional
    public SprintResponseDTO startSprint(UUID projectId, UUID userId, UUID sprintId, SprintStartRequestDTO request) {
        validatePOorSM(userId, projectId);

        Sprint sprint = sprintRepository.findByIdAndProject_Id(sprintId, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sprint not found"));

        // STATE MACHINE: Only PLANNING → ACTIVE
        if (sprint.getStatus() != SprintStatus.PLANNING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sprint must be in PLANNING state to start");
        }

        sprint.setStatus(SprintStatus.ACTIVE);
        sprint.setStartedAt(LocalDateTime.now());

        Sprint saved = sprintRepository.save(sprint);
        return toResponse(saved);
    }

    @Transactional
    public SprintResponseDTO completeSprint(UUID projectId, UUID userId, UUID sprintId, SprintStartRequestDTO request) {
        validatePOorSM(userId, projectId);

        Sprint sprint = sprintRepository.findByIdAndProject_Id(sprintId, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sprint not found"));

        // STATE MACHINE: Only ACTIVE → CLOSED
        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sprint must be in ACTIVE state to complete");
        }

        sprint.setStatus(SprintStatus.CLOSED);
        sprint.setClosedAt(LocalDateTime.now());

        // Move uncompleted items back to backlog
        List<BacklogItem> items = backlogItemRepository.findAllBySprintIdOrderByBacklogOrderAsc(sprintId);
        for (BacklogItem item : items) {
            if (item.getStatus() != BacklogItemStatus.DONE) {
                item.setSprint(null);
                backlogItemRepository.save(item);
            }
        }

        Sprint saved = sprintRepository.save(sprint);
        return toResponse(saved);
    }

    @Transactional
    public SprintResponseDTO updateSprint(UUID projectId, UUID userId, UUID sprintId, UpdateSprintRequestDTO request) {
        validatePOorSM(userId, projectId);

        Sprint sprint = sprintRepository.findByIdAndProject_Id(sprintId, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sprint not found"));

        // Only allow updates in PLANNING state
        if (sprint.getStatus() != SprintStatus.PLANNING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Can only update sprints in PLANNING state");
        }

        if (request.title() != null) {
            sprint.setTitle(request.title().trim());
        }
        if (request.sprintGoal() != null) {
            sprint.setSprintGoal(request.sprintGoal().trim());
        }
        if (request.startDate() != null) {
            sprint.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            sprint.setEndDate(request.endDate());
        }

        if (sprint.getStartDate() != null && sprint.getEndDate() != null
                && !sprint.getEndDate().isAfter(sprint.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }

        Sprint saved = sprintRepository.save(sprint);
        return toResponse(saved);
    }

    // ============ DELETE ============

    @Transactional
    public void deleteSprint(UUID projectId, UUID userId, UUID sprintId) {
        validatePOorSM(userId, projectId);

        Sprint sprint = sprintRepository.findByIdAndProject_Id(sprintId, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sprint not found"));

        // Only allow deletion in PLANNING state
        if (sprint.getStatus() != SprintStatus.PLANNING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Can only delete sprints in PLANNING state");
        }

        // Move all items back to backlog
        List<BacklogItem> items = backlogItemRepository.findAllBySprintIdOrderByBacklogOrderAsc(sprintId);
        for (BacklogItem item : items) {
            item.setSprint(null);
            backlogItemRepository.save(item);
        }

        sprintRepository.delete(sprint);
    }

    // ============ HELPER CONVERSIONS ============

    private SprintResponseDTO toResponse(Sprint sprint) {
        List<BacklogItem> items = backlogItemRepository.findAllBySprintIdOrderByBacklogOrderAsc(sprint.getId());
        List<BacklogItemResponseDTO> itemDTOs = items.stream()
                .map(this::toBacklogItemResponse)
                .collect(Collectors.toList());

        int totalPoints = items.stream()
                .mapToInt(item -> item.getEstimatedPoints() != null ? item.getEstimatedPoints() : 0)
                .sum();

        int completedPoints = items.stream()
                .filter(item -> item.getStatus() == BacklogItemStatus.DONE)
                .mapToInt(item -> item.getEstimatedPoints() != null ? item.getEstimatedPoints() : 0)
                .sum();

        return new SprintResponseDTO(
                sprint.getId(),
                sprint.getTitle(),
                sprint.getSprintGoal(),
                sprint.getStatus(),
                sprint.getStartDate(),
                sprint.getEndDate(),
                sprint.getStartedAt(),
                sprint.getClosedAt(),
                totalPoints,
                completedPoints,
                totalPoints - completedPoints,
                calculateVelocity(sprint),
                itemDTOs
        );
    }

    private BacklogItemResponseDTO toBacklogItemResponse(BacklogItem item) {
        return new BacklogItemResponseDTO(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getStatus(),
                item.getType(),
                item.getPriority(),
                item.getDifficulty(),
                item.getEstimatedPoints(),
                item.getEstimatedHours(),
                item.getAssigneeId(),
                item.getParent() != null ? item.getParent().getId() : null,
                item.getSprint() != null ? item.getSprint().getId() : null,
                item.getCreatedById(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCompletedAt()
        );
    }

    private Float calculateVelocity(Sprint sprint) {
        if (sprint.getStatus() != SprintStatus.CLOSED) {
            return null;
        }

        List<BacklogItem> items = backlogItemRepository.findAllBySprintIdOrderByBacklogOrderAsc(sprint.getId());
        int completedPoints = items.stream()
                .filter(item -> item.getStatus() == BacklogItemStatus.DONE)
                .mapToInt(item -> item.getEstimatedPoints() != null ? item.getEstimatedPoints() : 0)
                .sum();

        return (float) completedPoints;
    }
}
