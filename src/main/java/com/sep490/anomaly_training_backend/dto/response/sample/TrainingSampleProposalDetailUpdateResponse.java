package com.sep490.anomaly_training_backend.dto.response.sample;

import com.sep490.anomaly_training_backend.enums.ProposalType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingSampleProposalDetailUpdateResponse {
    Long id;
    Long trainingSampleId;
    ProposalType proposalType;
    Long processId;
    List<Long> productIds;
    Long defectId;
    String categoryName;
    String trainingSampleCode;
    String trainingDescription;
    String note;
}
