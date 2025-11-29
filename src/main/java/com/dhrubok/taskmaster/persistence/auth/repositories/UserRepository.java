package com.dhrubok.taskmaster.persistence.auth.repositories;

import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.auth.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
