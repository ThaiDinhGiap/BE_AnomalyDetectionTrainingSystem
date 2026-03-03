package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TrainingSampleProposalResponse {
    Long id;
    String productLineName;
    ReportStatus status;
    Long teamLeadId;
    String teamLeadName;
    LocalDateTime createdDate;
}
