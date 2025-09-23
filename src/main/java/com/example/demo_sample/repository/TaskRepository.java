package com.example.demo_sample.repository;

import com.example.demo_sample.domain.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Integer>, JpaSpecificationExecutor<TaskEntity> {

    // Lấy Task theo requirementName (dùng @Query)
    @Query("""
            SELECT t FROM TaskEntity t
            WHERE t.requirementName = :requirementName
           """)
    TaskEntity getByRequirementName(String requirementName);

    // Đếm số task theo từng taskTypeId (native SQL)
    @Query(value = "SELECT task_type_id, COUNT(*) FROM tasks GROUP BY task_type_id", nativeQuery = true)
    List<Object[]> countTasksGroupByType();

}
