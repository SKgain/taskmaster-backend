package com.dhrubok.taskmaster.persistence.features.projectmember.repositories;

import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.features.project.entities.Project;
import com.dhrubok.taskmaster.persistence.features.projectmember.entities.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, String> {
    List<ProjectMember> findByUser(User user);

    List<ProjectMember> findByProject(Project project);

    Optional<ProjectMember> findByProjectAndUser(Project project, User user);

    boolean existsByProjectAndUser(Project project, User user);

    Long countByProject(Project project);

    void deleteByProject(Project project);

    List<ProjectMember> findByProjectId(String id);

    boolean existsByUserIdAndProjectId(String userId, String projectId);
}
