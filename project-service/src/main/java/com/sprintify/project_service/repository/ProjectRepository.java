package com.sprintify.project_service.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sprintify.project_service.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
}