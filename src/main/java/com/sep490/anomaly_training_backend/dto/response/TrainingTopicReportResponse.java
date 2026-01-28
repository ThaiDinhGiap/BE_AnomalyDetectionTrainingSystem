package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainingTopicReportResponse {
    Long id;
    String groupName;
    ReportStatus status;

}
