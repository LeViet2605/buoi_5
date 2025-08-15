package com.example.demo_sample.domain.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PaginateTaskDTO {

    private Integer id;

    private String requirementName;

    private String taskTypeName;

    private LocalDate date;

    private Double duration;

    private String assignee;

    private String reviewer;
}
