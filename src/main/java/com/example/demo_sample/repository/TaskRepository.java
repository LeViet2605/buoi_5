package com.example.demo_sample.repository;

import com.example.demo_sample.domain.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Integer> {

    @Query("""
            SELECT t FROM TaskEntity t
            WHERE t.requirementName = :requirementName
           """)
    TaskEntity getByRequirementName(String requirementName);
}