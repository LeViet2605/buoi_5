package com.example.demo_sample.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@Setter
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Integer taskId;

    @Column(name = "requirement_name")
    private String requirementName;

    @Column(name = "task_type_id")
    private Integer taskTypeId;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "plan_from")
    private Double planFrom;

    @Column(name = "plan_to")
    private Double planTo;

    @Column(name = "assignee")
    private String assignee;

    @Column(name = "reviewer")
    private String reviewer;

    @Column(name = "created_at")
    @Setter(value = AccessLevel.NONE)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}