package com.sep490.anomaly_training_backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DefectInProcess {
    Long processId;
    String processCode;
    String processName;
    Long totalDefects;
    Long totalDefectiveGood;
    Long totalClaim;
    Long totalStartledClaim;
}
