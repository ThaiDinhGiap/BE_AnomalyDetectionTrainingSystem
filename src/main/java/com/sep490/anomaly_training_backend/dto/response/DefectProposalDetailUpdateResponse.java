package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.ProposalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectProposalDetailUpdateResponse {
    Long id;
    Long defectId;
    ProposalType proposalType;
    String defectDescription;
    Long processId;
    String processName;
    LocalDate detectedDate;
    String note;
    String originCause;
    String outflowCause;
    String causePoint;
    Boolean deleteFlag;
    String originMeasures;
    String outflowMeasures;
    String defectType;
}
