package com.sep490.anomaly_training_backend.controller;

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
        return export(ExportEntityType.DEFECT_PROPOSAL, id);
    }

    @GetMapping("/defect-proposals")
    @PreAuthorize("hasAuthority('defect_proposal.view')")
    @Operation(summary = "Export danh sách đề xuất lỗi")
    public ResponseEntity<byte[]> exportDefectProposalList() {
        return export(ExportEntityType.DEFECT_PROPOSAL, null);
    }

    // ── Training Sample Proposal ────────────────────────────────────────────

    @GetMapping("/training-sample-proposals/{id}")
    @PreAuthorize("hasAuthority('training_sample_proposal.view')")
    @Operation(summary = "Export phiếu đề xuất mẫu đào tạo")
    public ResponseEntity<byte[]> exportTrainingSampleProposal(@PathVariable Long id) {
        return export(ExportEntityType.TRAINING_SAMPLE_PROPOSAL, id);
    }

    @GetMapping("/training-sample-proposals")
    @PreAuthorize("hasAuthority('training_sample_proposal.view')")
    @Operation(summary = "Export danh sách đề xuất mẫu đào tạo")
    public ResponseEntity<byte[]> exportTrainingSampleProposalList() {
        return export(ExportEntityType.TRAINING_SAMPLE_PROPOSAL, null);
    }

    // ── Training Plan ───────────────────────────────────────────────────────

    @GetMapping("/training-plans/{id}")
    @PreAuthorize("hasAuthority('training_plan.view')")
    @Operation(summary = "Export kế hoạch đào tạo")
    public ResponseEntity<byte[]> exportTrainingPlan(@PathVariable Long id) {
        return export(ExportEntityType.TRAINING_PLAN, id);
    }

    @GetMapping("/training-plans")
    @PreAuthorize("hasAuthority('training_plan.view')")
    @Operation(summary = "Export danh sách kế hoạch đào tạo")
    public ResponseEntity<byte[]> exportTrainingPlanList() {
        return export(ExportEntityType.TRAINING_PLAN, null);
    }

    // ── Training Result ─────────────────────────────────────────────────────

    @GetMapping("/training-results/{id}")
    @PreAuthorize("hasAuthority('training_result.view')")
    @Operation(summary = "Export kết quả đào tạo")
    public ResponseEntity<byte[]> exportTrainingResult(@PathVariable Long id) {
        return export(ExportEntityType.TRAINING_RESULT, id);
    }

    @GetMapping("/training-results")
    @PreAuthorize("hasAuthority('training_result.view')")
    @Operation(summary = "Export danh sách kết quả đào tạo")
    public ResponseEntity<byte[]> exportTrainingResultList() {
        return export(ExportEntityType.TRAINING_RESULT, null);
    }

    // ── Training Sample Review ──────────────────────────────────────────────

    @GetMapping("/training-sample-reviews/{id}")
    @PreAuthorize("hasAuthority('training_sample_review.view')")
    @Operation(summary = "Export phiếu rà soát mẫu đào tạo")
    public ResponseEntity<byte[]> exportTrainingSampleReview(@PathVariable Long id) {
        return export(ExportEntityType.TRAINING_SAMPLE_REVIEW, id);
    }

    @GetMapping("/training-sample-reviews")
    @PreAuthorize("hasAuthority('training_sample_review.view')")
    @Operation(summary = "Export danh sách rà soát mẫu đào tạo")
    public ResponseEntity<byte[]> exportTrainingSampleReviewList() {
        return export(ExportEntityType.TRAINING_SAMPLE_REVIEW, null);
    }

    // ── Helper ──────────────────────────────────────────────────────────────

    private ResponseEntity<byte[]> export(ExportEntityType type, Long id) {
        ExportService.ExportResult result = (id != null)
                ? exportService.exportSingle(type, id)
                : exportService.exportList(type);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_MEDIA_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + result.fileName() + "\"")
                .body(result.data());
    }
}
