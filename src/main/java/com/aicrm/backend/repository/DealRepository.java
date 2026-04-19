package com.aicrm.backend.repository;

import com.aicrm.backend.model.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {
    List<Deal> findByStage(String stage);
    List<Deal> findByAssignedToId(Long userId);
    List<Deal> findByLeadId(Long leadId);
}