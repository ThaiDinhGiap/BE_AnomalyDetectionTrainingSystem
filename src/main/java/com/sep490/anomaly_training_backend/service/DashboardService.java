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

    // ==================== MNG Dashboard ====================

    MngOrgStats getMngOrgStats(Long sectionId, Long lineId);

    List<MngPendingApprovalItem> getMngPendingApprovals(Long sectionId, Long lineId);

    List<MngTrainingProgressPoint> getMngTrainingProgress(Long sectionId, Long lineId, Integer month, Integer year);

    SvKpiData getMngKpi(Long sectionId, Long lineId, Integer year, Integer month);

    List<SvTrainingEffectivenessPoint> getMngTrainingEffectiveness(Long sectionId, Long lineId, Integer months);

    List<TrainingExecutionPoint> getMngTrainingExecution(Long sectionId, Long lineId, Integer year, Integer month);

    List<SkillCertificateItem> getMngSkillCertificates(Long sectionId, Long lineId);

    List<DefectTrendPoint> getMngDefectTrend(Long sectionId, Long lineId);

    List<StageDistribution> getMngDefectByProcess(Long sectionId, Long lineId);

    List<StageDistribution> getMngSampleByProcess(Long sectionId, Long lineId);
}
