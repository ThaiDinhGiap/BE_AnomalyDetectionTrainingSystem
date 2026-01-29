package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.response.DefectReportDetailResponse;

import java.util.List;

public interface DefectReportDetailService {
    List<DefectReportDetailResponse> getDefectReportDetails(Long defectReportId);
}
