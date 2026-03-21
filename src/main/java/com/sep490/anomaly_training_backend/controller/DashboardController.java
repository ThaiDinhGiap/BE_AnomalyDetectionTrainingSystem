package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.dashboard.*;
import com.sep490.anomaly_training_backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "API Dashboard cho Team Leader")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get KPI summary for dashboard")
    @GetMapping("/kpi")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<KpiData>> getKpi(
            @RequestParam(value = "lineId", required = false) Long lineId,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {
        KpiData data = dashboardService.getKpi(lineId, year, month);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Get rejected reports")
    @GetMapping("/rejected-reports")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<RejectedReportItem>>> getRejectedReports(
            @RequestParam(value = "lineId", required = false) Long lineId,
            @RequestParam(value = "type", required = false) Integer type) {
        List<RejectedReportItem> data = dashboardService.getRejectedReports(lineId, type);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Get training tasks (today/failed/missed)")
    @GetMapping("/training-tasks")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<TrainingTaskData>> getTrainingTasks(
            @RequestParam(value = "lineId", required = false) Long lineId) {
        TrainingTaskData data = dashboardService.getTrainingTasks(lineId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Get training heatmap data for a year")
    @GetMapping("/training-heatmap")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getTrainingHeatmap(
            @RequestParam(value = "lineId", required = false) Long lineId,
            @RequestParam(value = "year", required = false) Integer year) {
        Map<String, Integer> data = dashboardService.getTrainingHeatmap(lineId, year);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Get training execution chart data for a month")
    @GetMapping("/training-execution")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<TrainingExecutionPoint>>> getTrainingExecution(
            @RequestParam(value = "lineId", required = false) Long lineId,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {
        List<TrainingExecutionPoint> data = dashboardService.getTrainingExecution(lineId, year, month);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Get process flow for a product line")
    @GetMapping("/process-flow")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<ProcessFlowItem>>> getProcessFlow(
            @RequestParam(value = "lineId", required = false) Long lineId) {
        List<ProcessFlowItem> data = dashboardService.getProcessFlow(lineId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Get skill certificate statistics by process")
    @GetMapping("/skill-certificates")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<SkillCertificateItem>>> getSkillCertificates(
            @RequestParam(value = "lineId", required = false) Long lineId) {
        List<SkillCertificateItem> data = dashboardService.getSkillCertificates(lineId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Get defect trend over time")
    @GetMapping("/defect-trend")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<DefectTrendPoint>>> getDefectTrend(
            @RequestParam(value = "lineId", required = false) Long lineId) {
        List<DefectTrendPoint> data = dashboardService.getDefectTrend(lineId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Get defect distribution by process")
    @GetMapping("/defect-by-process")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<StageDistribution>>> getDefectByProcess(
            @RequestParam(value = "lineId", required = false) Long lineId) {
        List<StageDistribution> data = dashboardService.getDefectByProcess(lineId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Get training sample distribution by process")
    @GetMapping("/sample-by-process")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<StageDistribution>>> getSampleByProcess(
            @RequestParam(value = "lineId", required = false) Long lineId) {
        List<StageDistribution> data = dashboardService.getSampleByProcess(lineId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // ==================== SV Dashboard Endpoints ====================

    @Operation(summary = "[SV] Get product lines for group dropdown")
    @GetMapping("/sv/lines")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<SvDashboardLineResponse>>> getSvLines(
            @RequestParam("groupId") Long groupId) {
        List<SvDashboardLineResponse> data = dashboardService.getSvLines(groupId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "[SV] Get todo list with KPIs and pending approvals")
    @GetMapping("/sv/todo")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<SvTodoData>> getSvTodo(
            @RequestParam("groupId") Long groupId,
            @RequestParam(value = "lineId", required = false) Long lineId) {
        SvTodoData data = dashboardService.getSvTodo(groupId, lineId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "[SV] Get team benchmark comparison")
    @GetMapping("/sv/team-benchmark")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<SvTeamBenchmark>>> getSvTeamBenchmark(
            @RequestParam("groupId") Long groupId,
            @RequestParam(value = "lineId", required = false) Long lineId,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year) {
        List<SvTeamBenchmark> data = dashboardService.getSvTeamBenchmark(groupId, lineId, month, year);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "[SV] Get defect count by operation/process")
    @GetMapping("/sv/defect-by-operation")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<SvDefectByOperation>>> getSvDefectByOperation(
            @RequestParam("groupId") Long groupId,
            @RequestParam(value = "lineId", required = false) Long lineId) {
        List<SvDefectByOperation> data = dashboardService.getSvDefectByOperation(groupId, lineId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "[SV] Get top defect hotspot processes")
    @GetMapping("/sv/defect-hotspot")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<SvDefectHotspot>>> getSvDefectHotspot(
            @RequestParam("groupId") Long groupId,
            @RequestParam(value = "lineId", required = false) Long lineId) {
        List<SvDefectHotspot> data = dashboardService.getSvDefectHotspot(groupId, lineId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "[SV] Get KPI cards data")
    @GetMapping("/sv/kpi")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<SvKpiData>> getSvKpi(
            @RequestParam("groupId") Long groupId,
            @RequestParam(value = "lineId", required = false) Long lineId,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {
        SvKpiData data = dashboardService.getSvKpi(groupId, lineId, year, month);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "[SV] Get employee watchlist (skill issues)")
    @GetMapping("/sv/watchlist")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<SvWatchlistItem>>> getSvWatchlist(
            @RequestParam("groupId") Long groupId,
            @RequestParam(value = "lineId", required = false) Long lineId) {
        List<SvWatchlistItem> data = dashboardService.getSvWatchlist(groupId, lineId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "[SV] Get recent evaluation activity")
    @GetMapping("/sv/recent-activity")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<SvRecentActivityItem>>> getSvRecentActivity(
            @RequestParam("groupId") Long groupId,
            @RequestParam(value = "lineId", required = false) Long lineId) {
        List<SvRecentActivityItem> data = dashboardService.getSvRecentActivity(groupId, lineId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "[SV] Get training status donut chart data")
    @GetMapping("/sv/training-status")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<SvTrainingStatusItem>>> getSvTrainingStatus(
            @RequestParam("groupId") Long groupId,
            @RequestParam(value = "lineId", required = false) Long lineId) {
        List<SvTrainingStatusItem> data = dashboardService.getSvTrainingStatus(groupId, lineId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "[SV] Get training effectiveness chart data")
    @GetMapping("/sv/training-effectiveness")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<SvTrainingEffectivenessPoint>>> getSvTrainingEffectiveness(
            @RequestParam("groupId") Long groupId,
            @RequestParam(value = "lineId", required = false) Long lineId,
            @RequestParam(value = "months", required = false) Integer months) {
        List<SvTrainingEffectivenessPoint> data = dashboardService.getSvTrainingEffectiveness(groupId, lineId, months);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "[SV] Get top training samples with fail rate")
    @GetMapping("/sv/top-training-samples")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<SvTopTrainingSampleItem>>> getSvTopTrainingSamples(
            @RequestParam("groupId") Long groupId,
            @RequestParam(value = "lineId", required = false) Long lineId) {
        List<SvTopTrainingSampleItem> data = dashboardService.getSvTopTrainingSamples(groupId, lineId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
