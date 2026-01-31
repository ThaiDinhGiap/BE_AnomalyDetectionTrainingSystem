package com.sep490.anomaly_training_backend.dto.request;

import com.sep490.anomaly_training_backend.enums.ReportType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateTrainingTopicReportDetailRequest {
    Long trainingTopicId;
    ReportType reportType;
    Long processId;
    Long defectId;
    String categoryName;
    String trainingSample;
    String trainingDetail;
    String note;
}
