package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.ProposalType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TrainingSampleProposalDetailResponse {
    Long id;
    Long trainingSampleId;
    ProposalType proposalType;
    ProductResponse product;
    Long processId;
    String processName;
    Long defectId;
    String defectDescription;
    String categoryName;
    String trainingSampleCode;
    String trainingDescription;
    String note;
    List<String> attachmentUrls;
}
