package com.sprintify.project_service.controller;

import java.util.UUID;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid; 
import com.sprintify.project_service.dto.CreateProjectRequestDTO;
import com.sprintify.project_service.dto.InviteRequestDTO;
import com.sprintify.project_service.dto.InviteResponseDTO;
import com.sprintify.project_service.dto.InviteDecisionRequestDTO;
import com.sprintify.project_service.dto.ProjectMemberResponseDTO;
import com.sprintify.project_service.dto.ProjectResponseDTO;
import com.sprintify.project_service.service.ProjectService;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

	private final ProjectService projectService;

	public ProjectController(ProjectService projectService) {
		this.projectService = projectService;
	}

	@PostMapping("/create")
	public ResponseEntity<ProjectResponseDTO> create(
			@RequestHeader("X-User-Id") String userId,
			@Valid @RequestBody CreateProjectRequestDTO request
	) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(projectService.createProject(request, parseUserId(userId)));
	}

	@PostMapping("/{projectId}/invites")
	public ResponseEntity<Void> invite(
			@PathVariable UUID projectId,
			@RequestHeader("X-User-Id") String userId,
			@Valid @RequestBody InviteRequestDTO request
	) {
		projectService.inviteToProject(projectId, parseUserId(userId), request);
		return ResponseEntity.ok().build();
	}

	//Get all pending invites for the authenticated user
	@GetMapping("/invites/me")
	public ResponseEntity<List<InviteResponseDTO>> getMyInvites(
			@RequestHeader("X-User-Id") String userId
	) {
		return ResponseEntity.ok(projectService.getMyPendingInvites(parseUserId(userId)));
	}


	@PutMapping("/{projectId}/invites/respond")
	public ResponseEntity<Void> respondToInvite(
			@PathVariable UUID projectId,
			@RequestHeader("X-User-Id") String userId,
			@Valid @RequestBody InviteDecisionRequestDTO request
	) {

		//return the updated invite status to the user
		 projectService.respondToInvite(projectId, parseUserId(userId), request);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{projectId}")
	public ResponseEntity<Void> delete(
			@PathVariable UUID projectId,
			@RequestHeader("X-User-Id") String userId
	) {
		projectService.deleteProject(projectId, parseUserId(userId));
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{projectId}/members")
	public ResponseEntity<List<ProjectMemberResponseDTO>> getProjectMembers(
			@PathVariable UUID projectId
	) {
		return ResponseEntity.ok(projectService.getProjectMembers(projectId));
	}

	@DeleteMapping("/{projectId}/members/{memberId}")
	public ResponseEntity<Void> removeMember(
			@PathVariable UUID projectId,
			@PathVariable UUID memberId,
			@RequestHeader("X-User-Id") String userId
	) {
		projectService.deleteMember(projectId, memberId, parseUserId(userId));
		return ResponseEntity.noContent().build();
	}

	private UUID parseUserId(String userId) {
		try {
			return UUID.fromString(userId);
		} catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user identifier");
		}
	}
}
