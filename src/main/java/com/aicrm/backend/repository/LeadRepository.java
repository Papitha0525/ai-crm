package com.aicrm.backend.repository;

import com.aicrm.backend.model.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    // ===============================
    // BASIC FILTERS
    // ===============================

    // Status மூலம் Leads
    List<Lead> findByStatus(String status);

    // City மூலம் Leads
    List<Lead> findByCity(String city);

    // ===============================
    // ASSIGNMENT FILTERS
    // ===============================

    // User ID மூலம் assigned leads
    List<Lead> findByAssignedToId(Long userId);

    // User Email மூலம் assigned leads
    List<Lead> findByAssignedToEmail(String email);

    // ===============================
    // NAME SEARCH
    // ===============================

    // Single lead by name
    Lead findByNameIgnoreCase(String name);

    // Multiple same name leads
    List<Lead> findAllByNameIgnoreCase(String name);

    // Contains search
    List<Lead> findByNameContainingIgnoreCase(String keyword);

    // ===============================
    // DASHBOARD COUNTS
    // ===============================

    long countByStatus(String status);

    long countByDealStatus(String dealStatus);

    long countByAssignedToId(Long userId);

    // ===============================
    // REPORT FILTERS
    // ===============================

    List<Lead> findByDealStatus(String dealStatus);

    List<Lead> findBySource(String source);
}