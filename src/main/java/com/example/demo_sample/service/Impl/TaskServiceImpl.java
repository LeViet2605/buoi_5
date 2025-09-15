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
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    //    Sử dụng bean
    @Override
    public TaskEntity getById(Integer id) {
        return taskRepository.findById(id).orElse(null);
    }

    @Override
    public TaskEntity create(CreateTaskDTO data) {
        Assert.notNull(data, "cannot be null");
        TaskEntity newTaskEntity = convertFrom(data);
        return taskRepository.save(newTaskEntity); // sẽ có id
    }

    public TaskEntity convertFrom(CreateTaskDTO data) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setRequirementName(data.getRequirementName());
        taskEntity.setTaskTypeId(data.getTaskTypeId());
        taskEntity.setDate(data.getDate());
        taskEntity.setPlanFrom(data.getPlanFrom());
        taskEntity.setPlanTo(data.getPlanTo());
        taskEntity.setAssignee(data.getAssignee());
        taskEntity.setReviewer(data.getReviewer());
        return taskEntity;
    }

    @Override
    public void update(Integer id, UpdateTaskDTO data) {
        Assert.notNull(id, "ID không được null");
        Assert.notNull(data, "Dữ liệu không được null");

        TaskEntity existing = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task không tồn tại"));

        // Giả sử bạn đã có các setter
        existing.setRequirementName(data.getRequirementName());
        existing.setTaskTypeId(data.getTaskTypeId());
        existing.setDate(data.getDate());
        existing.setPlanFrom(data.getPlanFrom());
        existing.setPlanTo(data.getPlanTo());
        existing.setAssignee(data.getAssignee());
        existing.setReviewer(data.getReviewer());
        existing.setUpdatedAt(LocalDateTime.now());

        taskRepository.save(existing);
    }


    @Override
    public void delete(Integer id) {
        Assert.notNull(id, "id cannot be null");

        TaskEntity taskEntity = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        taskRepository.delete(taskEntity);
    }

    @Override
    public Page<TaskEntity> paginate(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }


    @Override
    public List<String> countTasksGroupByType() {
        List<Object[]> results = taskRepository.countTasksGroupByType();
        return results.stream()
                .map(row -> "TaskTypeId: " + row[0] + " - Count: " + row[1])
                .toList();
    }

    @Override
    public Page<TaskEntity> search(Integer id, Integer taskTypeId, String requirementName, Pageable pageable) {
        return taskRepository.findAll((root, query, cb) -> {
            // danh sách điều kiện
            var predicates = new java.util.ArrayList<>();

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
    }



}