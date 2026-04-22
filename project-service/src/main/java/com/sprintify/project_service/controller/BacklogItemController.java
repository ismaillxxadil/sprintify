package com.sprintify.project_service.controller;

import com.sprintify.project_service.dto.*;
import com.sprintify.project_service.service.BacklogItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/backlog-items")
@RequiredArgsConstructor
public class BacklogItemController {

    private final BacklogItemService backlogItemService;

    /**
     * POST /api/v1/projects/{projectId}/backlog-items
     * Create Epic/Story (PO/SM) or Task/Bug (DEV)
     */
    @PostMapping
    public ResponseEntity<BacklogItemResponseDTO> createBacklogItem(
            @PathVariable UUID projectId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateBacklogItemRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(backlogItemService.createBacklogItem(projectId, parseUserId(userId), request));
    }

    /**
     * GET /api/v1/projects/{projectId}/backlog-items
     * List backlog items with optional sprint filter
     */
    @GetMapping
    public ResponseEntity<Page<BacklogItemResponseDTO>> listBacklogItems(
            @PathVariable UUID projectId,
            @RequestParam(required = false) UUID sprintId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(backlogItemService.listBacklogItems(projectId, sprintId, pageable));
    }

    /**
     * GET /api/v1/projects/{projectId}/backlog-items/{itemId}
     * Get item details with hierarchy
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<BacklogItemDetailResponseDTO> getBacklogItem(
            @PathVariable UUID projectId,
            @PathVariable UUID itemId
    ) {
        return ResponseEntity.ok(backlogItemService.getBacklogItemWithChildren(projectId, itemId));
    }

    /**
     * PATCH /api/v1/projects/{projectId}/backlog-items/{itemId}
     * General update: title, description, priority, difficulty, estimatedPoints, estimatedHours
     */
    @PatchMapping("/{itemId}")
    public ResponseEntity<BacklogItemResponseDTO> updateBacklogItem(
            @PathVariable UUID projectId,
            @PathVariable UUID itemId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateBacklogItemRequestDTO request
    ) {
        return ResponseEntity.ok(backlogItemService.updateBacklogItem(projectId, parseUserId(userId), itemId, request));
    }

    /**
     * PATCH /api/v1/projects/{projectId}/backlog-items/{itemId}/status
     * Update status (Todo → In Progress → Done)
     */
    @PatchMapping("/{itemId}/status")
    public ResponseEntity<BacklogItemResponseDTO> updateStatus(
            @PathVariable UUID projectId,
            @PathVariable UUID itemId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateBacklogItemStatusRequestDTO request
    ) {
        return ResponseEntity.ok(backlogItemService.updateStatus(projectId, parseUserId(userId), itemId, request));
    }

    /**
     * PATCH /api/v1/projects/{projectId}/backlog-items/{itemId}/assign
     * Assign item to user (DEV self-assign or PO/SM can assign)
     */
    @PatchMapping("/{itemId}/assign")
    public ResponseEntity<BacklogItemResponseDTO> assignItem(
            @PathVariable UUID projectId,
            @PathVariable UUID itemId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody(required = false) AssignBacklogItemRequestDTO request
    ) {
        return ResponseEntity.ok(backlogItemService.assignItem(projectId, parseUserId(userId), itemId, request));
    }

    /**
     * PATCH /api/v1/projects/{projectId}/backlog-items/{itemId}/sprint
     * Move item to sprint or back to backlog
     */
    @PatchMapping("/{itemId}/sprint")
    public ResponseEntity<BacklogItemResponseDTO> moveToSprint(
            @PathVariable UUID projectId,
            @PathVariable UUID itemId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody MoveToSprintRequestDTO request
    ) {
        return ResponseEntity.ok(backlogItemService.moveToSprint(projectId, parseUserId(userId), itemId, request));
    }

    /**
     * DELETE /api/v1/projects/{projectId}/backlog-items/{itemId}
     * Delete item and cascade children
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteBacklogItem(
            @PathVariable UUID projectId,
            @PathVariable UUID itemId,
            @RequestHeader("X-User-Id") String userId
    ) {
        backlogItemService.deleteBacklogItem(projectId, parseUserId(userId), itemId);
        return ResponseEntity.noContent().build();
    }

    private UUID parseUserId(String userId) {
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid user identifier"
            );
        }
    }
}
