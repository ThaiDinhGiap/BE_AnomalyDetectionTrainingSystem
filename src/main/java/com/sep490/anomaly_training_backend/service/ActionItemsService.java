package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.response.ExpiringSkillResponse;
import com.sep490.anomaly_training_backend.dto.response.FailedTrainingResponse;
import com.sep490.anomaly_training_backend.dto.response.PendingSignatureResponse;

import java.util.List;

public interface ActionItemsService {
    List<PendingSignatureResponse> getPendingSignatures(Long lineId);
    List<FailedTrainingResponse> getFailedTrainings(Long lineId);
    List<ExpiringSkillResponse> getExpiringSkills(Long lineId);
}
