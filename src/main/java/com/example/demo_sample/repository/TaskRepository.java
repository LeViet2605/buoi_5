package com.example.demo_sample.repository;

import com.example.demo_sample.domain.TaskEntity;
import com.example.demo_sample.domain.dto.TaskTypeCountDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Integer>, JpaSpecificationExecutor<TaskEntity> {

    Page<TaskEntity> findByTaskId(Integer taskId, Pageable pageable);

    Page<TaskEntity> findByRequirementNameContainingIgnoreCase(String requirementName, Pageable pageable);

    Page<TaskEntity> findByTaskIdAndRequirementNameContainingIgnoreCase(
            Integer taskId, String requirementName, Pageable pageable
    );

    // Đếm số task theo từng taskTypeId
    @Query("SELECT new com.example.demo_sample.domain.dto.TaskTypeCountDTO(t.taskTypeId, '', COUNT(t)) " +
            "FROM TaskEntity t GROUP BY t.taskTypeId")
    List<TaskTypeCountDTO> countTasksGroupByType();


}
