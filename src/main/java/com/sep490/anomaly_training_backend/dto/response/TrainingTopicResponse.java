package com.sep490.anomaly_training_backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainingTopicResponse {
    Long id;
    Long processId;
    String processName;
    Long defectId;
    String defectName;
    String trainingSample;
    String trainingDetail;
    String note;
}
