package com.sep490.anomaly_training_backend.dto.request;

import com.sep490.anomaly_training_backend.enums.ProposalType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class DefectProposalDetailUpdateRequest {
    Long id;
    ProposalType proposalType;
    Long defectId;
    String defectDescription;
    Long processId;
    LocalDate detectedDate;
    Boolean isEscaped;
    String note;
    String originCause;
    String outflowCause;
    String causePoint;
    Boolean deleteFlag;
}
