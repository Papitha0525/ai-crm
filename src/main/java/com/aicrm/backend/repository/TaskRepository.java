package com.aicrm.backend.repository;

import com.aicrm.backend.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStatus(String status);
    List<Task> findByAssignedToId(Long userId);
    List<Task> findByRelatedTypeAndRelatedId(String relatedType, Long relatedId);
}