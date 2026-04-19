package com.aicrm.backend.service;

import com.aicrm.backend.model.Task;
import com.aicrm.backend.model.User;
import com.aicrm.backend.repository.TaskRepository;
import com.aicrm.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    // Task create பண்ணு
    public Task createTask(String title, String dueDate,
                           String relatedType, Long relatedId,
                           Long assignedToId) {
        Task task = new Task();
        task.setTitle(title);
        task.setDueDate(LocalDate.parse(dueDate));
        task.setStatus("PENDING");
        task.setRelatedType(relatedType);
        task.setRelatedId(relatedId);

        if (assignedToId != null) {
            User user = userRepository.findById(assignedToId)
                    .orElse(null);
            task.setAssignedTo(user);
        }

        return taskRepository.save(task);
    }

    // எல்லா Tasks எடு
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // Status மூலம் Tasks எடு
    public List<Task> getTasksByStatus(String status) {
        return taskRepository.findByStatus(status);
    }

    // Task complete பண்ணு
    public Task completeTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("Task not found!"));
        task.setStatus("DONE");
        return taskRepository.save(task);
    }

    // Task delete பண்ணு
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}