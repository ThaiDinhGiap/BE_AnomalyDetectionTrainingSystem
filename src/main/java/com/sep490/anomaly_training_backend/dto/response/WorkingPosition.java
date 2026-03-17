package com.sep490.anomaly_training_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkingPosition {
    Long sectionId;
    String sectionName;
    Long groupId;
    String groupName;
    Long teamId;
    String teamName;
    String teamLeadName;
    String teamLeadCode;
    String finalInspectionName;
    String finalInspectionCode;
    Long productLineId;
    String productLineName;
    List<ProcessResponse> processes;
}
