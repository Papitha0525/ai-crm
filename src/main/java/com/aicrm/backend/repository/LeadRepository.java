package com.aicrm.backend.repository;

import com.aicrm.backend.model.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    // Status மூலம் Leads எடு
    List<Lead> findByStatus(String status);

    // City மூலம் Leads எடு
    List<Lead> findByCity(String city);

    // User மூலம் Leads எடு
    List<Lead> findByAssignedToId(Long userId);

    // ✅ Single lead by name
    Lead findByNameIgnoreCase(String name);

    // ✅ Multiple leads by name
    List<Lead> findAllByNameIgnoreCase(String name);
}