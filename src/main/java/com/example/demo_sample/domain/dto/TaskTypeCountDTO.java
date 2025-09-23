package com.example.demo_sample.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskTypeCountDTO {
    private Integer typeId;
    private String typeName;
    private Long count;
}
