package com.example.demo_sample.domain.dto;

import com.example.demo_sample.Common.TaskTypeStatus;
import lombok.Data;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
public class CreateTaskDTO {

    private String requirementName;
    private String assignee;
    private String reviewer;
    private Integer taskTypeId;
    private LocalDate date;
    private Double planFrom;
    private Double planTo;
    private MultipartFile file;

    public Map<String, String> validate() {
        Map<String, String> errors = new HashMap<>();

        if (!StringUtils.hasText(requirementName)) {
            errors.put("requirementName", "Requirement name không được để trống");
        }

        if (!StringUtils.hasText(assignee)) {
            errors.put("assignee", "Assignee không được để trống");
        }

        if (!StringUtils.hasText(reviewer)) {
            errors.put("reviewer", "Reviewer không được để trống");
        }

        if (taskTypeId == null || !TaskTypeStatus.getAllStatusValues().contains(taskTypeId)) {
            errors.put("taskTypeId", "TaskTypeID phải nằm trong khoảng hợp lệ");
        }

        if (date == null) {
            errors.put("date", "Date không được để trống");
        }

        if (planFrom == null) {
            errors.put("planFrom", "PlanFrom không được để trống");
        }

        if (planTo == null) {
            errors.put("planTo", "PlanTo không được để trống");
        }

        if (planFrom != null && planTo != null) {
            if (planFrom < 8.0 || planTo > 17.5 || planFrom >= planTo) {
                errors.put("time", "Thời gian phải nằm trong khoảng [8.0 - 17.5] và planFrom < planTo");
            }
        }

        if (file != null && file.isEmpty()) {
            errors.put("file", "File không được để trống nếu có gửi file");
        }

        return errors;
    }
}