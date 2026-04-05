package com.sep490.anomaly_training_backend.dto.response.sample;

import com.sep490.anomaly_training_backend.dto.approval.RejectFeedbackJson;
import com.sep490.anomaly_training_backend.dto.response.ProductResponse;
import com.sep490.anomaly_training_backend.dto.response.defect.DefectResponse;
import com.sep490.anomaly_training_backend.enums.ProposalType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TrainingSampleProposalDetailResponse {
    Long trainingSampleProposalDetailId;
    Long trainingSampleProposalId;
    Long trainingSampleId;
    ProposalType proposalType;
    List<ProductResponse> products;
    DefectResponse defect;
    Long processId;
    String processName;
    Long defectId;
    String defectDescription;
    String categoryName;
    String trainingSampleCode;
    String trainingDescription;
    String note;
    List<String> attachmentUrls;
    RejectFeedbackJson rejectFeedback;
}
