package com.sprintify.project_service.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Project {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@EqualsAndHashCode.Include
	private UUID id;

	@NotBlank
	@Size(min = 2, max = 150)
	@Column(nullable = false, length = 150)
	private String name;

	@Size(min = 2, max = 4000)
	@Column(length = 4000)
	private String description;

	@NotBlank
	@Size(min = 2, max = 50)
	@Column(nullable = false, length = 50)
	private String state;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Builder.Default
	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	private List<ProjectMember> members = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	private List<Sprint> sprints = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	private List<BacklogItem> backlogItems = new ArrayList<>();

	@PrePersist
	public void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
