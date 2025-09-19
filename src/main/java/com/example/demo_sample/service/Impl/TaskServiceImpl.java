package com.example.demo_sample.service.Impl;

import com.example.demo_sample.domain.TaskEntity;
import com.example.demo_sample.domain.dto.CreateTaskDTO;
import com.example.demo_sample.domain.dto.UpdateTaskDTO;
import com.example.demo_sample.repository.TaskRepository;
import com.example.demo_sample.service.TaskService;
import io.jsonwebtoken.lang.Assert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public ResponseEntity<?> getById(Integer id) {
        TaskEntity task = taskRepository.findById(id).orElse(null);
        if (task == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Task không tồn tại"));
        }
        return ResponseEntity.ok(task);
    }

    @Override
    public ResponseEntity<?> create(CreateTaskDTO data) {
        Map<String, String> errorMap = data.validate();
        if (!errorMap.isEmpty()) {
            return ResponseEntity.badRequest().body(errorMap);
        }

        TaskEntity newTaskEntity = new TaskEntity();
        newTaskEntity.setRequirementName(data.getRequirementName());
        newTaskEntity.setTaskTypeId(data.getTaskTypeId());
        newTaskEntity.setDate(data.getDate());
        newTaskEntity.setPlanFrom(data.getPlanFrom());
        newTaskEntity.setPlanTo(data.getPlanTo());
        newTaskEntity.setAssignee(data.getAssignee());
        newTaskEntity.setReviewer(data.getReviewer());

        TaskEntity created = taskRepository.save(newTaskEntity);

        return ResponseEntity.status(201).body(Map.of(
                "message", "Tạo task thành công",
                "id Task", created.getTaskId()
        ));
    }

    @Override
    public ResponseEntity<?> update(Integer id, UpdateTaskDTO data) {
        Map<String, String> errorMap = data.validate();
        if (!errorMap.isEmpty()) {
            return ResponseEntity.badRequest().body(errorMap);
        }

        TaskEntity existing = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task không tồn tại"));

        existing.setRequirementName(data.getRequirementName());
        existing.setTaskTypeId(data.getTaskTypeId());
        existing.setDate(data.getDate());
        existing.setPlanFrom(data.getPlanFrom());
        existing.setPlanTo(data.getPlanTo());
        existing.setAssignee(data.getAssignee());
        existing.setReviewer(data.getReviewer());
        existing.setUpdatedAt(LocalDateTime.now());

        taskRepository.save(existing);

        return ResponseEntity.ok(Map.of("message", "Update task thành công"));
    }

    @Override
    public ResponseEntity<?> delete(Integer id, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Bạn chưa đăng nhập"));
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (!isAdmin) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền xóa task"));
        }

        TaskEntity taskEntity = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        taskRepository.delete(taskEntity);

        return ResponseEntity.ok(Map.of("message", "Xóa Task thành công"));
    }

    @Override
    public ResponseEntity<?> paginate(Pageable pageable) {
        Page<TaskEntity> tasks = taskRepository.findAll(pageable);
        return ResponseEntity.ok(tasks);
    }

    @Override
    public ResponseEntity<?> search(Integer id, Integer taskTypeId, String requirementName, Pageable pageable) {
        Page<TaskEntity> tasks = taskRepository.findAll((root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            if (id != null) {
                predicates.add(cb.equal(root.get("taskId"), id));
            }
            if (taskTypeId != null) {
                predicates.add(cb.equal(root.get("taskTypeId"), taskTypeId));
            }
            if (requirementName != null && !requirementName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("requirementName")), "%" + requirementName.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable);

        if (tasks.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy task nào"));
        }
        return ResponseEntity.ok(tasks);
    }

    @Override
    public ResponseEntity<?> countAllTypes() {
        List<Object[]> results = taskRepository.countTasksGroupByType();
        List<String> mapped = results.stream()
                .map(row -> "TaskTypeId: " + row[0] + " - Count: " + row[1])
                .toList();
        return ResponseEntity.ok(Map.of("message", mapped));
    }
}
