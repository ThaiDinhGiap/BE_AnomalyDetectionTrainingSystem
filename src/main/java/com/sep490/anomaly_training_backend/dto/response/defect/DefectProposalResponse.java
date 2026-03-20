package com.sep490.anomaly_training_backend.dto.response.defect;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DefectProposalResponse {
    Long id;
    ReportStatus status;
    Long productLineId;
    String productLineName;
    Long teamLeadId;
    String teamLeadName;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
