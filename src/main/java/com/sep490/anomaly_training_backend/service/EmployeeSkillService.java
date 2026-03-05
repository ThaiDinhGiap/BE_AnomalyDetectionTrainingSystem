package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.EmployeeSkillRequest;
import com.sep490.anomaly_training_backend.dto.response.EmployeeSkillResponse;

public interface EmployeeSkillService {
    EmployeeSkillResponse createEmployeeSkill(EmployeeSkillRequest employeeSkillRequest);

    EmployeeSkillResponse updateEmployeeSkillByTeamLead(Long id, EmployeeSkillRequest employeeSkillRequest);

    void deleteEmployeeSkill(Long id);

}
