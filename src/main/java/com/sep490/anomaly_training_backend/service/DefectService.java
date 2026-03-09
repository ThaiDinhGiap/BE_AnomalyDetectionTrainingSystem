package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.response.DefectResponse;

import java.util.List;

public interface DefectService {
    List<DefectResponse> getDefectBySupervisor(Long userId);

    List<DefectResponse> getDefectByProductLine(Long productLineId);

    DefectResponse getDefectById(Long id);

}
