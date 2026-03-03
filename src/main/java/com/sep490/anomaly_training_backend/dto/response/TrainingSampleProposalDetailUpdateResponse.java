package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.ProposalType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingSampleProposalDetailUpdateResponse {
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
}
