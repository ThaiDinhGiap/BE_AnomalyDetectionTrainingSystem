package com.sep490.anomaly_training_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkingPosition {
    Long sectionId;
    String sectionName;
    Long managerId;
    String managerName;
    String managerCode;
    Long groupId;
    String groupName;
    Long supervisorId;
    String supervisorName;
    String supervisorCode;
    Long teamId;
    String teamName;
    String teamLeadName;
    String teamLeadCode;
    String finalInspectionName;
    String finalInspectionCode;
    Long productLineId;
    String productLineName;
}
