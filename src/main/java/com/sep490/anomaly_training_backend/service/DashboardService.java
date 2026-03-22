package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.response.dashboard.*;

import java.util.List;
import java.util.Map;

public interface DashboardService {

    KpiData getKpi(Long lineId, Integer year, Integer month);

    List<RejectedReportItem> getRejectedReports(Long lineId, Integer type);

    TrainingTaskData getTrainingTasks(Long lineId);

    Map<String, Integer> getTrainingHeatmap(Long lineId, Integer year);

    List<TrainingExecutionPoint> getTrainingExecution(Long lineId, Integer year, Integer month);

    List<ProcessFlowItem> getProcessFlow(Long lineId);

    List<SkillCertificateItem> getSkillCertificates(Long lineId);

    List<DefectTrendPoint> getDefectTrend(Long lineId);

    List<StageDistribution> getDefectByProcess(Long lineId);

    List<StageDistribution> getSampleByProcess(Long lineId);

    // ==================== SV Dashboard ====================

    List<SvDashboardLineResponse> getSvLines(Long groupId);

    SvTodoData getSvTodo(Long groupId, Long lineId);

    List<SvTeamBenchmark> getSvTeamBenchmark(Long groupId, Long lineId, Integer month, Integer year);

    List<SvDefectByOperation> getSvDefectByOperation(Long groupId, Long lineId);

    List<SvDefectHotspot> getSvDefectHotspot(Long groupId, Long lineId);

    SvKpiData getSvKpi(Long groupId, Long lineId, Integer year, Integer month);

    List<SvWatchlistItem> getSvWatchlist(Long groupId, Long lineId);

    List<SvRecentActivityItem> getSvRecentActivity(Long groupId, Long lineId);

    List<SvTrainingStatusItem> getSvTrainingStatus(Long groupId, Long lineId);

    List<SvTrainingEffectivenessPoint> getSvTrainingEffectiveness(Long groupId, Long lineId, Integer months);

    List<SvTopTrainingSampleItem> getSvTopTrainingSamples(Long groupId, Long lineId);

    List<TrainingExecutionPoint> getSvTrainingExecution(Long groupId, Long lineId, Integer year, Integer month);

    List<SkillCertificateItem> getSvSkillCertificates(Long groupId, Long lineId);

    List<DefectTrendPoint> getSvDefectTrend(Long groupId, Long lineId);

    List<StageDistribution> getSvDefectByProcess(Long groupId, Long lineId);

    List<StageDistribution> getSvSampleByProcess(Long groupId, Long lineId);
}
