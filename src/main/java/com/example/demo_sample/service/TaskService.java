package com.example.demo_sample.service;

import com.example.demo_sample.domain.TaskEntity;
import com.example.demo_sample.domain.dto.CreateTaskDTO;
import com.example.demo_sample.domain.dto.UpdateTaskDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public interface TaskService {

    ResponseEntity<?> getById(Integer id);

    ResponseEntity<?> create(CreateTaskDTO data);

    ResponseEntity<?> update(Integer id, UpdateTaskDTO data);

    ResponseEntity<?> delete(Integer id, Authentication authentication);

    ResponseEntity<?> paginate(Pageable pageable);

    ResponseEntity<?> search(Integer id, String requirementName, Pageable pageable);


    ResponseEntity<?> countAllTypes();
}
