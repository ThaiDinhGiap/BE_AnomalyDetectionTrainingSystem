package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DefectProposalResponse {
    Long id;
    ReportStatus status;
    String groupName;
    Long teamLeadId;
    String teamLeadName;
    LocalDateTime createdDate;
}
