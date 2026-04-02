package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.EmployeeSkillRequest;
import com.sep490.anomaly_training_backend.dto.response.EmployeeSkillResponse;
import com.sep490.anomaly_training_backend.dto.response.skill_matrix.SkillMatrixResponse;
import com.sep490.anomaly_training_backend.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EmployeeSkillService {
    EmployeeSkillResponse createEmployeeSkill(EmployeeSkillRequest employeeSkillRequest);

    EmployeeSkillResponse updateEmployeeSkillByTeamLead(Long id, EmployeeSkillRequest employeeSkillRequest);

    void deleteEmployeeSkill(Long id);

    SkillMatrixResponse getSkillMatrix(Long teamId, Long lineId, List<Long> employeeIds, List<Long> processIds);

    void importSkillMatrix(MultipartFile file, User currentUser);

    List<EmployeeSkillResponse> getEmployeeSkillsByEmployeeId(Long employeeId);
}
