package com.example.demo_sample.controller.dto;

import com.example.demo_sample.domain.TaskEntity;
import com.example.demo_sample.domain.dto.CreateTaskDTO;
import com.example.demo_sample.domain.dto.UpdateTaskDTO;
import com.example.demo_sample.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    //DI
    public record ErrorResponse(String error) {}
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

        TaskEntity created = taskService.create(task);

        return ResponseEntity.status(201).body(Map.of(
                "message", "Tạo task thành công",
                "id Task", created.getTaskId()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Integer id, @RequestBody UpdateTaskDTO task) {
        Map<String, String> errorMap = task.validate();
        if (!errorMap.isEmpty()) {
            return ResponseEntity.badRequest().body(errorMap);
        }
        taskService.update(id, task);
        return ResponseEntity.ok(Map.of("message", "Update task thành công"));
    }

//    @DeleteMapping("/{id}")
//    public void deleteTask(@PathVariable Integer id,  Authentication authentication) {
//        taskService.delete(id);
//    }



     @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(
            @PathVariable Integer id,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(new ErrorResponse("Bạn chưa đăng nhập"));
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (!isAdmin) {
            return ResponseEntity.status(403).body(new ErrorResponse("Bạn không có quyền xóa task"));
        }
        taskService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Xóa Task thành công"));
    }

    @GetMapping("")
    public Page<TaskEntity> getTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return taskService.paginate(pageable);
    }
}
