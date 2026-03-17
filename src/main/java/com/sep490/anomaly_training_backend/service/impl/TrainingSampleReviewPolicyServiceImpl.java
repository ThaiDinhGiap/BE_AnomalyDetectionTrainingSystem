package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.TrainingSampleReviewPolicyResponse;
import com.sep490.anomaly_training_backend.repository.TrainingSampleReviewPolicyRepository;
import com.sep490.anomaly_training_backend.service.TrainingSampleReviewPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingSampleReviewPolicyServiceImpl implements TrainingSampleReviewPolicyService {
    private final TrainingSampleReviewPolicyRepository trainingSampleReviewPolicyRepository;

    @Override
    public List<TrainingSampleReviewPolicyResponse> getTrainingSampleReviewPoliciesByProductLine(Long productLineId) {
        return trainingSampleReviewPolicyRepository.findByProductLineIdAndDeleteFlagFalse(productLineId).stream()
                .map(policy -> TrainingSampleReviewPolicyResponse.builder()
                        .id(policy.getId())
                        .policyCode(policy.getPolicyCode())
                        .effectiveDate(policy.getEffectiveDate())
                        .expirationDate(policy.getExpirationDate())
                        .status(policy.getStatus())
                        .description(policy.getDescription())
                        .build())
                .toList();
    }
}
