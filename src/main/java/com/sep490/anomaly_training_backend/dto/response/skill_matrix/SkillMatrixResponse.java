package com.sep490.anomaly_training_backend.dto.response.skill_matrix;

import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import lombok.Data;

import java.util.List;

@Data
public class SkillMatrixResponse {
    private List<ProcessResponse> processes;
    private List<EmployeeSkillsDto> employeeSkills;
}
