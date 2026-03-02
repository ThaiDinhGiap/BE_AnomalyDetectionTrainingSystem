package com.sep490.anomaly_training_backend.service.scoring;

import com.sep490.anomaly_training_backend.dto.scoring.ComputedMetricResponse;
import com.sep490.anomaly_training_backend.enums.PolicyEntityType;

import java.util.List;

public interface ComputedMetricService {

    List<ComputedMetricResponse> getMetricsByEntityType(PolicyEntityType entityType);
}
