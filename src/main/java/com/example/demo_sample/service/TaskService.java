package com.example.demo_sample.service;

import com.example.demo_sample.domain.TaskEntity;
import com.example.demo_sample.domain.dto.CreateTaskDTO;
import com.example.demo_sample.domain.dto.UpdateTaskDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;


public interface TaskService {

    public Page<TaskEntity> paginate(Pageable pageable);

    public TaskEntity getById(Integer id);

    public TaskEntity create(CreateTaskDTO data);

    public void update(Integer id, UpdateTaskDTO data);

    public void delete(Integer id);

    public List<String> countTasksGroupByType();

    public Page<TaskEntity> search(Integer id, Integer taskTypeId, String requirementName, Pageable pageable);

}

