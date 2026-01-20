package com.dhrubok.taskmaster.persistence.system.repositories;

import com.dhrubok.taskmaster.persistence.system.entities.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {
    SystemConfig findFirstByOrderByIdDesc();
}