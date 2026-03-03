package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.ProposalType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainingSampleProposalDetailResponse {
    Long id;
    String processName;
    String defectDescription;
    String categoryName;
    ProposalType proposalType;
    String trainingSample;
    String trainingDetail;
    String note;
}
