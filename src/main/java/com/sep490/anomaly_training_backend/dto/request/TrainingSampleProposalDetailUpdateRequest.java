package com.sep490.anomaly_training_backend.dto.request;

import com.sep490.anomaly_training_backend.enums.ProposalType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainingSampleProposalDetailUpdateRequest {
    Long id;
    Long trainingSampleProposalId;
    Long trainingSampleId;
    ProposalType proposalType;
    Long processId;
    Long productId;
    Long defectId;
    String categoryName;
    String trainingSampleCode;
    String trainingDescription;
    String note;
    Boolean deleteFlag;
}
