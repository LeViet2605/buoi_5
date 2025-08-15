package com.example.demo_sample.controller.dto;

import com.example.demo_sample.domain.TaskEntity;
import com.example.demo_sample.domain.dto.CreateTaskDTO;
import com.example.demo_sample.domain.dto.UpdateTaskDTO;
import com.example.demo_sample.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    //DI
    private final TaskService taskService;
    //Tạo bean

    @GetMapping("/{id}")
    public TaskEntity getTaskById(@PathVariable Integer id) {
        return taskService.getById(id);
    }


    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody CreateTaskDTO task) {
        Map<String, String> errorMap = task.validate();
        if (!errorMap.isEmpty()) {
            return ResponseEntity.badRequest().body(errorMap);
        }
        taskService.create(task);
        return ResponseEntity.ok().body(null);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Integer id, @RequestBody UpdateTaskDTO task) {
        Map<String, String> errorMap = task.validate();
        if (!errorMap.isEmpty()) {
            return ResponseEntity.badRequest().body(errorMap);
        }
        taskService.update(id, task);
        return ResponseEntity.ok().body(null);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Integer id) {
        taskService.delete(id);
    }

    @GetMapping("")
    public Page<TaskEntity> getTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return taskService.paginate(pageable);
    }
}
