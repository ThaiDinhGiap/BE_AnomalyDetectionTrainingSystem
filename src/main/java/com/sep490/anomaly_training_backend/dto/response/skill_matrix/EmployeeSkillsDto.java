package com.sep490.anomaly_training_backend.dto.response.skill_matrix;

import lombok.Data;

import java.util.Map;

@Data
public class EmployeeSkillsDto {
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private Map<Long, SkillStatusDto> skills; // Key: processId
}
