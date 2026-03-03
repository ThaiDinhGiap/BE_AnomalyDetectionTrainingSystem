package com.sep490.anomaly_training_backend.dto.request;

import com.sep490.anomaly_training_backend.enums.ProposalType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class CreateDefectProposalDetailRequest {
    Long defectId;
    ProposalType proposalType;
    String defectDescription;
    Long processId;
    LocalDate detectedDate;
    String note;
}
