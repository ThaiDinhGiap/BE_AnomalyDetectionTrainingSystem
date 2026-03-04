package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.ProposalType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class DefectProposalDetailResponse {
    Long id;
    ProposalType proposalType;
    String defectProposalDescription;
    String processName;
    LocalDate detectedDate;
    String note;
    String originCause;
    String outflowCause;
    String causePoint;
}
