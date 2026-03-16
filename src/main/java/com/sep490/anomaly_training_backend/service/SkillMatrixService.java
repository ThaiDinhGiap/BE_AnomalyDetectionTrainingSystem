package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.response.skill_matrix.SkillMatrixResponse;

public interface SkillMatrixService {
    SkillMatrixResponse getSkillMatrix(Long teamId, Long lineId);
}
