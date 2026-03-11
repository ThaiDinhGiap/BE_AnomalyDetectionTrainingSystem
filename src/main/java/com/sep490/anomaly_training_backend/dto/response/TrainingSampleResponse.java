package com.sep490.anomaly_training_backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainingSampleResponse {
    Long trainingSampleId;
    Long processId;
    String processName;
    Long productId;
    String productCode;
    Long defectId;
    String defectDescription;
    String trainingSampleCode;
    String categoryName;
    String trainingDescription;
    String note;
}
