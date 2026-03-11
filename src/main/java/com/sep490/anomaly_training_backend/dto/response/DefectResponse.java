package com.sep490.anomaly_training_backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class DefectResponse {
    Long defectId;
    String defectCode;
    String defectDescription;
    Long processId;
    String processName;
    LocalDate detectedDate;
    Boolean isEscaped;
    String note;
    String originCause;
    String outflowCause;
    String causePoint;
}
