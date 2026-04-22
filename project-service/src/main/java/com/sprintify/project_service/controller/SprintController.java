package com.sprintify.project_service.controller;

import com.sprintify.project_service.dto.*;
import com.sprintify.project_service.service.SprintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/sprints")
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;

    /**
     * POST /api/v1/projects/{projectId}/sprints
     * Create new sprint (PO/SM only)
     */
    @PostMapping
    public ResponseEntity<SprintResponseDTO> createSprint(
            @PathVariable UUID projectId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateSprintRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sprintService.createSprint(projectId, parseUserId(userId), request));
    }

    /**
     * GET /api/v1/projects/{projectId}/sprints
     * List all sprints in project
     */
    @GetMapping
    public ResponseEntity<List<SprintResponseDTO>> listSprints(
            @PathVariable UUID projectId
    ) {
        return ResponseEntity.ok(sprintService.listSprints(projectId));
    }

    /**
     * GET /api/v1/projects/{projectId}/sprints/{sprintId}
     * Get sprint details with items
     */
    @GetMapping("/{sprintId}")
    public ResponseEntity<SprintResponseDTO> getSprint(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId
    ) {
        return ResponseEntity.ok(sprintService.getSprint(projectId, sprintId));
    }

    /**
     * POST /api/v1/projects/{projectId}/sprints/{sprintId}/items
     * Add items to sprint (PO/SM only)
     */
    @PostMapping("/{sprintId}/items")
    public ResponseEntity<SprintResponseDTO> addItemsToSprint(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody AddItemsToSprintRequestDTO request
    ) {
        return ResponseEntity.ok(sprintService.addItemsToSprint(projectId, parseUserId(userId), sprintId, request));
    }

    /**
     * DELETE /api/v1/projects/{projectId}/sprints/{sprintId}/items/{itemId}
     * Remove item from sprint (PO/SM only, PLANNING state)
     */
    @DeleteMapping("/{sprintId}/items/{itemId}")
    public ResponseEntity<SprintResponseDTO> removeItemFromSprint(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @PathVariable UUID itemId,
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(sprintService.removeItemFromSprint(projectId, parseUserId(userId), sprintId, itemId));
    }

    /**
     * POST /api/v1/projects/{projectId}/sprints/{sprintId}/start
     * Start sprint (PO/SM only, PLANNING → ACTIVE)
     */
    @PostMapping("/{sprintId}/start")
    public ResponseEntity<SprintResponseDTO> startSprint(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody(required = false) SprintStartRequestDTO request
    ) {
        SprintStartRequestDTO req = request != null ? request : new SprintStartRequestDTO(null);
        return ResponseEntity.ok(sprintService.startSprint(projectId, parseUserId(userId), sprintId, req));
    }

    /**
     * POST /api/v1/projects/{projectId}/sprints/{sprintId}/complete
     * Complete sprint (PO/SM only, ACTIVE → CLOSED)
     */
    @PostMapping("/{sprintId}/complete")
    public ResponseEntity<SprintResponseDTO> completeSprint(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody(required = false) SprintStartRequestDTO request
    ) {
        SprintStartRequestDTO req = request != null ? request : new SprintStartRequestDTO(null);
        return ResponseEntity.ok(sprintService.completeSprint(projectId, parseUserId(userId), sprintId, req));
    }

    /**
     * PUT /api/v1/projects/{projectId}/sprints/{sprintId}
     * Update sprint metadata (PO/SM only, PLANNING state)
     */
    @PutMapping("/{sprintId}")
    public ResponseEntity<SprintResponseDTO> updateSprint(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateSprintRequestDTO request
    ) {
        return ResponseEntity.ok(sprintService.updateSprint(projectId, parseUserId(userId), sprintId, request));
    }

    /**
     * DELETE /api/v1/projects/{projectId}/sprints/{sprintId}
     * Delete sprint (PO/SM only, PLANNING state)
     */
    @DeleteMapping("/{sprintId}")
    public ResponseEntity<Void> deleteSprint(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @RequestHeader("X-User-Id") String userId
    ) {
        sprintService.deleteSprint(projectId, parseUserId(userId), sprintId);
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
