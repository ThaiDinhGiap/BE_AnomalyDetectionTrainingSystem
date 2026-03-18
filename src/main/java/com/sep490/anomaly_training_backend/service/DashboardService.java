package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.response.dashboard.*;

import java.util.List;
import java.util.Map;

public interface DashboardService {

    KpiData getKpi(Long lineId, Integer year, Integer month);

    List<RejectedReportItem> getRejectedReports(Long lineId, String type);

    TrainingTaskData getTrainingTasks(Long lineId);

    Map<String, Integer> getTrainingHeatmap(Long lineId, Integer year);

    List<TrainingExecutionPoint> getTrainingExecution(Long lineId, Integer year, Integer month);

    List<ProcessFlowItem> getProcessFlow(Long lineId);

    List<SkillCertificateItem> getSkillCertificates(Long lineId);

    List<DefectTrendPoint> getDefectTrend(Long lineId);

    List<StageDistribution> getDefectByProcess(Long lineId);

    List<StageDistribution> getSampleByProcess(Long lineId);
}
