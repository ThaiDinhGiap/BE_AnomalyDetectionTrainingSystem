package com.sep490.anomaly_training_backend.dto.request;

import com.sep490.anomaly_training_backend.enums.ProposalType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateTrainingSampleProposalDetailRequest {
    Long trainingSampleId;
    ProposalType proposalType;
    Long processId;
    Long defectId;
    String categoryName;
    String trainingSample;
    String trainingDetail;
    String note;
}
