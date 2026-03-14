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
    Long defectId;
    String defectDescription;
    Long processId;
    String processName;
    LocalDate detectedDate;
    Boolean isEscaped;
    String note;
    String originCause;
    String outflowCause;
    String causePoint;
    String originMeasures;
    String outflowMeasures;
    String defectType;
    
    // New fields
    String customer;
    Integer quantity;
    String conclusion;
    ProductResponse productResponse;
    

}
