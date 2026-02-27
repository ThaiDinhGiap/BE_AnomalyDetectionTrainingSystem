package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.CreateDefectReportRequest;
import com.sep490.anomaly_training_backend.dto.response.DefectReportResponse;

import java.util.List;

public interface DefectReportService {
    List<DefectReportResponse> getDefectReportByTeamLeadAndGroup(Long id, String username);

    void createDefectReport(CreateDefectReportRequest reportRequest);
}
