package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.ExportFilterRequest;
import com.sep490.anomaly_training_backend.enums.ExportEntityType;
import com.sep490.anomaly_training_backend.service.export.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exports")
@RequiredArgsConstructor
@Tag(name = "Export", description = "Xuất Excel cho các loại báo cáo")
public class ExportController {

    private final ExportService exportService;

    private static final String XLSX_MEDIA_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    // ── Defect Proposal ─────────────────────────────────────────────────────

    @GetMapping("/defect-proposals/{id}")
    @PreAuthorize("hasAuthority('defect_proposal.view')")
    @Operation(summary = "Export phiếu đề xuất lỗi")
    public ResponseEntity<byte[]> exportDefectProposal(@PathVariable Long id) {
        return exportSingle(ExportEntityType.DEFECT_PROPOSAL, id);
    }

    @PostMapping("/defect-proposals")
    @PreAuthorize("hasAuthority('defect_proposal.view')")
    @Operation(summary = "Export danh sách đề xuất lỗi (có filter)")
    public ResponseEntity<byte[]> exportDefectProposalList(@RequestBody(required = false) ExportFilterRequest filter) {
        return exportList(ExportEntityType.DEFECT_PROPOSAL, filter);
    }

    // ── Training Sample Proposal ────────────────────────────────────────────

    @GetMapping("/training-sample-proposals/{id}")
    @PreAuthorize("hasAuthority('training_sample_proposal.view')")
    @Operation(summary = "Export phiếu đề xuất mẫu đào tạo")
    public ResponseEntity<byte[]> exportTrainingSampleProposal(@PathVariable Long id) {
        return exportSingle(ExportEntityType.TRAINING_SAMPLE_PROPOSAL, id);
    }

    @PostMapping("/training-sample-proposals")
    @PreAuthorize("hasAuthority('training_sample_proposal.view')")
    @Operation(summary = "Export danh sách đề xuất mẫu đào tạo (có filter)")
    public ResponseEntity<byte[]> exportTrainingSampleProposalList(@RequestBody(required = false) ExportFilterRequest filter) {
        return exportList(ExportEntityType.TRAINING_SAMPLE_PROPOSAL, filter);
    }

    // ── Training Plan ───────────────────────────────────────────────────────

    @GetMapping("/training-plans/{id}")
    @PreAuthorize("hasAuthority('training_plan.view')")
    @Operation(summary = "Export kế hoạch đào tạo")
    public ResponseEntity<byte[]> exportTrainingPlan(@PathVariable Long id) {
        return exportSingle(ExportEntityType.TRAINING_PLAN, id);
    }

    @PostMapping("/training-plans")
    @PreAuthorize("hasAuthority('training_plan.view')")
    @Operation(summary = "Export danh sách kế hoạch đào tạo (có filter)")
    public ResponseEntity<byte[]> exportTrainingPlanList(@RequestBody(required = false) ExportFilterRequest filter) {
        return exportList(ExportEntityType.TRAINING_PLAN, filter);
    }

    // ── Training Result ─────────────────────────────────────────────────────

    @GetMapping("/training-results/{id}")
    @PreAuthorize("hasAuthority('training_result.view')")
    @Operation(summary = "Export kết quả đào tạo")
    public ResponseEntity<byte[]> exportTrainingResult(@PathVariable Long id) {
        return exportSingle(ExportEntityType.TRAINING_RESULT, id);
    }

    @PostMapping("/training-results")
    @PreAuthorize("hasAuthority('training_result.view')")
    @Operation(summary = "Export danh sách kết quả đào tạo (có filter)")
    public ResponseEntity<byte[]> exportTrainingResultList(@RequestBody(required = false) ExportFilterRequest filter) {
        return exportList(ExportEntityType.TRAINING_RESULT, filter);
    }

    // ── Training Sample Review ──────────────────────────────────────────────

    @GetMapping("/training-sample-reviews/{id}")
    @PreAuthorize("hasAuthority('training_sample_review.view')")
    @Operation(summary = "Export phiếu rà soát mẫu đào tạo")
    public ResponseEntity<byte[]> exportTrainingSampleReview(@PathVariable Long id) {
        return exportSingle(ExportEntityType.TRAINING_SAMPLE_REVIEW, id);
    }

    @PostMapping("/training-sample-reviews")
    @PreAuthorize("hasAuthority('training_sample_review.view')")
    @Operation(summary = "Export danh sách rà soát mẫu đào tạo (có filter)")
    public ResponseEntity<byte[]> exportTrainingSampleReviewList(@RequestBody(required = false) ExportFilterRequest filter) {
        return exportList(ExportEntityType.TRAINING_SAMPLE_REVIEW, filter);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private ResponseEntity<byte[]> exportSingle(ExportEntityType type, Long id) {
        ExportService.ExportResult result = exportService.exportSingle(type, id);
        return buildResponse(result);
    }

    private ResponseEntity<byte[]> exportList(ExportEntityType type, ExportFilterRequest filter) {
        ExportService.ExportResult result = exportService.exportList(type,
                filter != null ? filter : new ExportFilterRequest());
        return buildResponse(result);
    }

    private ResponseEntity<byte[]> buildResponse(ExportService.ExportResult result) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_MEDIA_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + result.fileName() + "\"")
                .body(result.data());
    }
}
