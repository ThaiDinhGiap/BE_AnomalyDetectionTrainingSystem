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
    Long groupId;
    String groupName;
    Long teamId;
    String teamName;
    String teamLead;
    Long productLineId;
    String productLineName;
}
