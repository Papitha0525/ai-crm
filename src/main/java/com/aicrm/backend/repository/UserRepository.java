package com.aicrm.backend.repository;

import com.aicrm.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // =====================================
    // LOGIN / AUTH
    // =====================================
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    Boolean existsByEmail(String email);

    Boolean existsByEmailIgnoreCase(String email);

    // =====================================
    // ROLE BASED
    // =====================================
    List<User> findByRole(String role);

    long countByRole(String role);

    // =====================================
    // NAME SEARCH
    // =====================================
    Optional<User> findByNameIgnoreCase(String name);

    List<User> findByNameContainingIgnoreCase(String name);

    Boolean existsByNameIgnoreCase(String name);

    // =====================================
    // ROLE + NAME
    // =====================================
    Optional<User> findByNameIgnoreCaseAndRole(
            String name,
            String role
    );

    List<User> findByRoleOrderByNameAsc(String role);

    // =====================================
    // REPORTS
    // =====================================
    long countByRoleIgnoreCase(String role);

    List<User> findAllByOrderByCreatedAtDesc();

    List<User> findAllByOrderByNameAsc();
}