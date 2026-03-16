package com.sep490.anomaly_training_backend.dto.response.skill_matrix;

import lombok.Data;

import java.util.List;

@Data
public class SkillMatrixResponse {
    private List<ProcessCompletionDto> processes;
    private List<EmployeeSkillsDto> employeeSkills;
}
