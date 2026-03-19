package com.sep490.anomaly_training_backend.service.approval.helper;

import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.model.DefectProposal;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposal;
import com.sep490.anomaly_training_backend.repository.DefectProposalRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApprovableEntityLoader {

    private final TrainingPlanRepository trainingPlanRepository;
    private final DefectProposalRepository defectProposalRepository;
    private final TrainingSampleProposalRepository trainingSampleProposalRepository;

    public Optional<Long> loadGroupId(ApprovalEntityType entityType, Long entityId) {
        return switch (entityType) {
            case TRAINING_PLAN -> trainingPlanRepository.findById(entityId).map(TrainingPlan::getGroupId);
            case DEFECT_PROPOSAL -> defectProposalRepository.findById(entityId).map(DefectProposal::getGroupId);
            case TRAINING_SAMPLE_PROPOSAL ->
                    trainingSampleProposalRepository.findById(entityId).map(TrainingSampleProposal::getGroupId);
            case TRAINING_RESULT -> Optional.empty();
        };
    }
}
