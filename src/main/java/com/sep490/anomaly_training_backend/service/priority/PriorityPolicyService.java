package com.sep490.anomaly_training_backend.service.priority;

import com.sep490.anomaly_training_backend.dto.scoring.ComputedMetricResponse;
import com.sep490.anomaly_training_backend.dto.scoring.PriorityPolicyListResponse;
import com.sep490.anomaly_training_backend.dto.scoring.PriorityPolicyRequest;
import com.sep490.anomaly_training_backend.dto.scoring.PriorityPolicyResponse;
import com.sep490.anomaly_training_backend.enums.PolicyEntityType;
import com.sep490.anomaly_training_backend.enums.PolicyStatus;

import java.util.List;

public interface PriorityPolicyService {

    PriorityPolicyResponse createPolicy(PriorityPolicyRequest request);

    PriorityPolicyResponse updatePolicy(Long id, PriorityPolicyRequest request);

    PriorityPolicyResponse getPolicy(Long id);

    List<PriorityPolicyListResponse> listPolicies(PolicyEntityType entityType, PolicyStatus status);

    void activatePolicy(Long id);

    void archivePolicy(Long id);

    void deletePolicy(Long id);

    List<ComputedMetricResponse> getAvailableMetrics(PolicyEntityType entityType);
}
