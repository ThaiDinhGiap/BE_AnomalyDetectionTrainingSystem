package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.EmployeeStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EmployeeResponse {
    private Long id;
    private String employeeCode;
    private String fullName;
    private EmployeeStatus status;

    private List<Long> teamIds;
    private String teamName;

    private String groupName;
    private String sectionName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    //New fields for skill and training summary
    List<EmployeeSkillResponse> skills;
    Integer totalTraining;
    Integer totalFail;
}