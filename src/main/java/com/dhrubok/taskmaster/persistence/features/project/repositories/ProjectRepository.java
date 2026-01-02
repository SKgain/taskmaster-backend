package com.dhrubok.taskmaster.persistence.features.project.repositories;

import com.dhrubok.taskmaster.persistence.features.project.entities.Project;
import com.dhrubok.taskmaster.persistence.features.project.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
    List<Project> findByStatus(ProjectStatus status);
    Long countByStatus(ProjectStatus status);

    List<Project> findByManagerUsername(String managerUsername);
    List<Project> findByCreatedBy(String email);

    List<Project> findAllByCreatedBy(String email);
}