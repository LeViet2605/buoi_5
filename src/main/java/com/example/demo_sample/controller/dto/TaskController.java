package com.example.demo_sample.controller.dto;

import com.example.demo_sample.domain.dto.CreateTaskDTO;
import com.example.demo_sample.domain.dto.UpdateTaskDTO;
import com.example.demo_sample.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Integer id) {
        return taskService.getById(id);
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody CreateTaskDTO task) {
        return taskService.create(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Integer id, @RequestBody UpdateTaskDTO task) {
        return taskService.update(id, task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Integer id, Authentication authentication) {
        return taskService.delete(id, authentication);
    }

    @GetMapping("")
    public ResponseEntity<?> getTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return taskService.paginate(PageRequest.of(page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchTasks(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) Integer taskTypeId,
            @RequestParam(required = false) String requirementName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return taskService.search(id, taskTypeId, requirementName, PageRequest.of(page, size));
    }

    @GetMapping("/count-all")
    public ResponseEntity<?> countAllTypes() {
        return taskService.countAllTypes();
    }
}
