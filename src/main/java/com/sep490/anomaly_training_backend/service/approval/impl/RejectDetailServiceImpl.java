package com.sep490.anomaly_training_backend.service.approval.impl;

import com.sep490.anomaly_training_backend.dto.approval.DetailFeedbackRequest;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.TrainingPlanService;
import com.sep490.anomaly_training_backend.service.approval.RejectDetailService;
import com.sep490.anomaly_training_backend.service.defect.DefectProposalDetailService;
import com.sep490.anomaly_training_backend.service.sample.TrainingSampleProposalDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RejectDetailServiceImpl implements RejectDetailService {

    private final DefectProposalDetailService defectProposalDetailService;
    private final TrainingSampleProposalDetailService trainingSampleProposalDetailService;
    private final TrainingPlanService trainingPlanService;

    @Override
    public void saveFeedback(ApprovalEntityType entityType, Long detailId, DetailFeedbackRequest request, User currentUser) {
        switch (entityType) {
            case DEFECT_PROPOSAL -> defectProposalDetailService.saveFeedback(detailId, request, currentUser);
            case TRAINING_SAMPLE_PROPOSAL ->
                    trainingSampleProposalDetailService.saveFeedback(detailId, request, currentUser);
            case TRAINING_PLAN -> trainingPlanService.saveFeedback(detailId, request, currentUser);
            default -> throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
    }
}
