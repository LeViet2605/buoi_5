package com.example.demo_sample.service.Impl;

import com.example.demo_sample.Common.ApiResponse;
import com.example.demo_sample.Common.CommonErrorCode;
import com.example.demo_sample.Common.TaskTypeStatus;
import com.example.demo_sample.domain.TaskEntity;
import com.example.demo_sample.domain.dto.CreateTaskDTO;
import com.example.demo_sample.domain.dto.TaskTypeCountDTO;
import com.example.demo_sample.domain.dto.UpdateTaskDTO;
import com.example.demo_sample.repository.TaskRepository;
import com.example.demo_sample.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

//    Tìm task theo id
    @Override
    public ResponseEntity<?> getById(Integer id) {
        TaskEntity task = taskRepository.findById(id).orElse(null);
        if (task == null) {
            return ApiResponse.error(CommonErrorCode.TASK_NOT_FOUND, 404);
        }
        return ResponseEntity.ok(task);
    }

    @Override
    public ResponseEntity<?> create(CreateTaskDTO data) {
        //check các giá trị ko dc null. bắt buộc phải nhập
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

        MultipartFile file = data.getFile(); // file trong DTO
        if (file != null && !file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get("uploads/" + fileName); // lưu vào folder uploads
            try {
                Files.createDirectories(filePath.getParent());
                file.transferTo(filePath.toFile());
                newTaskEntity.setFilePath(filePath.toString()); // lưu đường dẫn vào DB
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Lỗi khi lưu file: " + e.getMessage());
            }
        }


        TaskEntity created = taskRepository.save(newTaskEntity);

        return ApiResponse.successCreated(CommonErrorCode.CREATE_TASK_SUCCESS,
                Map.of("id Task", created.getTaskId()));
    }

    @Override
    public ResponseEntity<?> update(Integer id, UpdateTaskDTO data) {
        Map<String, String> errorMap = data.validate();
        if (!errorMap.isEmpty()) {
            return ResponseEntity.badRequest().body(errorMap);
        }

        TaskEntity existing = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, CommonErrorCode.TASK_NOT_FOUND.getMessage()));

        existing.setRequirementName(data.getRequirementName());
        existing.setTaskTypeId(data.getTaskTypeId());
        existing.setDate(data.getDate());
        existing.setPlanFrom(data.getPlanFrom());
        existing.setPlanTo(data.getPlanTo());
        existing.setAssignee(data.getAssignee());
        existing.setReviewer(data.getReviewer());
        existing.setUpdatedAt(LocalDateTime.now());

        taskRepository.save(existing);

        return ApiResponse.success(CommonErrorCode.UPDATE_TASK_SUCCESS, null);
    }

    @Override
    public ResponseEntity<?> delete(Integer id, Authentication authentication) {
        //Ktra token
        if (authentication == null) {
            return ApiResponse.error(CommonErrorCode.UNAUTHORIZED, 401);
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (!isAdmin) {
            return ApiResponse.error(CommonErrorCode.FORBIDDEN, 403);
        }

        TaskEntity taskEntity = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, CommonErrorCode.TASK_NOT_FOUND.getMessage()));

        taskRepository.delete(taskEntity);

        return ApiResponse.success(CommonErrorCode.DELETE_TASK_SUCCESS, null);
    }

    @Override
    public ResponseEntity<?> paginate(Pageable pageable) {
        Page<TaskEntity> tasks = taskRepository.findAll(pageable);
        return ResponseEntity.ok(tasks);
    }

    @Override
    public ResponseEntity<?> search(Integer id, String requirementName, Pageable pageable) {
        Page<TaskEntity> tasks; // biến chứa kết quả

        if (id != null && requirementName != null) {
            tasks = taskRepository.findByTaskIdAndRequirementNameContainingIgnoreCase(
                    id, requirementName, pageable);
        } else if (id != null) {
            tasks = taskRepository.findByTaskId(id, pageable);
        } else if (requirementName != null) {
            tasks = taskRepository.findByRequirementNameContainingIgnoreCase(requirementName, pageable);
        } else {
            tasks = taskRepository.findAll(pageable);
        }

        // kiểm tra rỗng
        if (tasks.isEmpty()) {
            return ApiResponse.error(CommonErrorCode.TASKS_NOT_FOUND, 404);
        }

        return ResponseEntity.ok(tasks);
    }



    @Override
    public ResponseEntity<?> countAllTypes() {
        List<TaskTypeCountDTO> results = taskRepository.countTasksGroupByType()
                .stream()
                .map(dto -> new TaskTypeCountDTO(
                        dto.getTypeId(),
                        TaskTypeStatus.getStatusName(dto.getTypeId()), // set lại typeName
                        dto.getCount()
                ))
                .toList();

        return ResponseEntity.ok(results);
    }


}
