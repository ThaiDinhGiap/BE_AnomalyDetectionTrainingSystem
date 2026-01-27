package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.response.DefectResponse;

import java.util.List;

public interface DefectService {

    List<DefectResponse> getDefects();

    List<DefectResponse> getDefectByProcess(Long processId);
}
