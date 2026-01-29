package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.response.DefectReportResponse;
import com.sep490.anomaly_training_backend.model.DefectReport;

import java.util.List;

public interface DefectReportService {
    List<DefectReportResponse> getDefectReportByTeamLeadAndGroup(Long id, String username);
}
