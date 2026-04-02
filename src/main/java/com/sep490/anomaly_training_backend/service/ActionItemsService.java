package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.response.ExpiringSkillResponse;
import com.sep490.anomaly_training_backend.dto.response.FailedTrainingResponse;
import com.sep490.anomaly_training_backend.dto.response.PendingSignatureResponse;

public interface ActionItemsService {
    PendingSignatureResponse getPendingSignatures(Long lineId);
    FailedTrainingResponse getFailedTrainings(Long lineId);
    ExpiringSkillResponse getExpiringSkills(Long lineId);
}
