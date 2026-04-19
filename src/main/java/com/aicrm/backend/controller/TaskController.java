package com.aicrm.backend.controller;

import com.aicrm.backend.model.Task;
import com.aicrm.backend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // Task create பண்ணு
    @PostMapping
    public ResponseEntity<Task> createTask(
            @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(taskService.createTask(
            request.get("title"),
            request.get("dueDate"),
            request.get("relatedType"),
            Long.parseLong(request.getOrDefault("relatedId", "0")),
            null
        ));
    }

    // எல்லா Tasks எடு
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    // Status மூலம் Tasks எடு
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Task>> getTasksByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(
            taskService.getTasksByStatus(status));
    }

    // Task complete பண்ணு
    @PutMapping("/{id}/complete")
    public ResponseEntity<Task> completeTask(
            @PathVariable Long id) {
        return ResponseEntity.ok(taskService.completeTask(id));
    }

    // Task delete பண்ணு
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(
            @PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok("Task deleted successfully!");
    }
}