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
            @RequestParam("lineId") Long lineId,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {
        KpiData data = dashboardService.getKpi(lineId, year, month);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Get rejected reports")
    @GetMapping("/rejected-reports")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<RejectedReportItem>>> getRejectedReports(
            @RequestParam("lineId") Long lineId,
            @RequestParam(value = "type", required = false) Integer type) {
        List<RejectedReportItem> data = dashboardService.getRejectedReports(lineId, type);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Get training tasks (today/failed/missed)")
    @GetMapping("/training-tasks")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<TrainingTaskData>> getTrainingTasks(
            @RequestParam("lineId") Long lineId) {
        TrainingTaskData data = dashboardService.getTrainingTasks(lineId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Get training heatmap data for a year")
    @GetMapping("/training-heatmap")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getTrainingHeatmap(
            @RequestParam("lineId") Long lineId,
            @RequestParam(value = "year", required = false) Integer year) {
        Map<String, Integer> data = dashboardService.getTrainingHeatmap(lineId, year);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Get training execution chart data for a month")
    @GetMapping("/training-execution")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<TrainingExecutionPoint>>> getTrainingExecution(
            @RequestParam("lineId") Long lineId,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {
        List<TrainingExecutionPoint> data = dashboardService.getTrainingExecution(lineId, year, month);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Get process flow for a product line")
    @GetMapping("/process-flow")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<ProcessFlowItem>>> getProcessFlow(
            @RequestParam("lineId") Long lineId) {
        List<ProcessFlowItem> data = dashboardService.getProcessFlow(lineId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Get skill certificate statistics by process")
    @GetMapping("/skill-certificates")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<SkillCertificateItem>>> getSkillCertificates(
            @RequestParam("lineId") Long lineId) {
        List<SkillCertificateItem> data = dashboardService.getSkillCertificates(lineId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Get defect trend over time")
    @GetMapping("/defect-trend")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<DefectTrendPoint>>> getDefectTrend(
            @RequestParam("lineId") Long lineId) {
        List<DefectTrendPoint> data = dashboardService.getDefectTrend(lineId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Get defect distribution by process")
    @GetMapping("/defect-by-process")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<StageDistribution>>> getDefectByProcess(
            @RequestParam("lineId") Long lineId) {
        List<StageDistribution> data = dashboardService.getDefectByProcess(lineId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Get training sample distribution by process")
    @GetMapping("/sample-by-process")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<List<StageDistribution>>> getSampleByProcess(
            @RequestParam("lineId") Long lineId) {
        List<StageDistribution> data = dashboardService.getSampleByProcess(lineId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
