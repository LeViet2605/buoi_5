package com.example.demo_sample.domain.dto;

import com.example.demo_sample.Common.TaskTypeStatus;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
public class UpdateTaskDTO {
    private String requirementName;
    private Integer taskTypeId;
    private LocalDate date;
    private Double planFrom;
    private Double planTo;
    private String assignee;
    private String reviewer;

    public Map<String, String> validate() {
        Map<String, String> errorMap = new HashMap<>();

        if (!StringUtils.hasText(requirementName)) {
            errorMap.put("requirementName", "Requirement Name không được để trống");
        }

        if (taskTypeId == null || !TaskTypeStatus.getAllStatusValues().contains(taskTypeId)) {
            errorMap.put("taskTypeId", "TaskTypeID phải nằm trong khoảng hợp lệ");
        }

        if (this.date == null) {
            errorMap.put("date", "Ngày không được để trống");
        }

        if (planFrom == null || planFrom < 8.0 || planFrom > 17.0) {
            errorMap.put("planFrom", "From phải nằm trong khoảng 8.0 đến 17.0");
        }

        if (planTo == null || planTo <= planFrom || planTo > 17.5) {
            errorMap.put("planTo", "To phải lớn hơn From và nhỏ hơn hoặc bằng 17.5");
        }

        if (!StringUtils.hasText(assignee)) {
            errorMap.put("assignee", "Assignee không được để trống");
        }

        if (!StringUtils.hasText(reviewer)) {
            errorMap.put("reviewer", "Reviewer không được để trống");
        }

        return errorMap;
    }
}
