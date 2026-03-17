package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.response.dashboard.*;

import java.util.List;
import java.util.Map;

public interface DashboardService {

    KpiData getKpi(Long lineId, int year, int month);

    List<RejectedReportItem> getRejectedReports(Long lineId, String type);

    TrainingTaskData getTrainingTasks(Long lineId);

    Map<String, Integer> getTrainingHeatmap(Long lineId, int year);

    List<TrainingExecutionPoint> getTrainingExecution(Long lineId, int year, int month);

    List<ProcessFlowItem> getProcessFlow(Long lineId);

    List<SkillCertificateItem> getSkillCertificates(Long lineId);

    List<DefectTrendPoint> getDefectTrend(Long lineId);

    List<StageDistribution> getDefectByProcess(Long lineId);

    List<StageDistribution> getSampleByProcess(Long lineId);
}
