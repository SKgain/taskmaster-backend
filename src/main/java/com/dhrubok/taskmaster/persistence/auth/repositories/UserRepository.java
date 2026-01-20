package com.dhrubok.taskmaster.persistence.auth.repositories;

import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.auth.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    List<User> findByRole(RoleType role);

    List<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String fullName,
            String email
    );

    List<User> findByRoleAndIsActiveTrue(RoleType role);

    Long countByRole(RoleType role);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String managerUsername);

    List<User> findByRoleAndCreatedBy(RoleType roleType, String managerEmail);

    List<User> findByRoleAndIsActiveTrueAndCreatedBy(RoleType roleType, String managerEmail);

    boolean existsByRole(RoleType role);

    List<User> findAllByRole(RoleType role);

    List<User> findAllByRoleAndIsActive(RoleType role, Boolean isActive);

    long countByIsActiveTrue();

    long countByIsEmailVerifiedFalse();

    List<User> findAllByIsActiveTrue();

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.broadCastTitle = :title, u.broadCastMessage = :message WHERE u.role = :role AND u.isActive = true")
    int updateBroadcastForRole(@Param("role") RoleType role, @Param("title") String title, @Param("message") String message);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.broadCastTitle = :title, u.broadCastMessage = :message WHERE u.isActive = true")
    int updateBroadcastForAll(@Param("title") String title, @Param("message") String message);
}
