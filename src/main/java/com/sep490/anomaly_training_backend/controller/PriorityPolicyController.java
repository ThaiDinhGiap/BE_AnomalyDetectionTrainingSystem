package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.scoring.ComputedMetricResponse;
import com.sep490.anomaly_training_backend.dto.scoring.PriorityPolicyListResponse;
import com.sep490.anomaly_training_backend.dto.scoring.PriorityPolicyRequest;
import com.sep490.anomaly_training_backend.dto.scoring.PriorityPolicyResponse;
import com.sep490.anomaly_training_backend.dto.scoring.PriorityScoringRequest;
import com.sep490.anomaly_training_backend.dto.scoring.PrioritySnapshotResponse;
import com.sep490.anomaly_training_backend.enums.PolicyEntityType;
import com.sep490.anomaly_training_backend.enums.PolicyStatus;
import com.sep490.anomaly_training_backend.mapper.PrioritySnapshotMapper;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.PrioritySnapshot;
import com.sep490.anomaly_training_backend.repository.EmployeeRepository;
import com.sep490.anomaly_training_backend.service.priority.PriorityPolicyService;
import com.sep490.anomaly_training_backend.service.priority.PriorityScoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/priority-policies")
@RequiredArgsConstructor
@Tag(name = "Priority Policy Management", description = "API for managing training priority policies and scoring snapshots")
public class PriorityPolicyController {

    private final PriorityPolicyService policyService;
    private final PriorityScoringService scoringService;
    private final PrioritySnapshotMapper snapshotMapper;
    private final EmployeeRepository employeeRepository;

    // ═══════════════════════════════════════════════════════════════════
    // POLICY CRUD
    // ═══════════════════════════════════════════════════════════════════

    @Operation(summary = "Create new priority policy")
    @PostMapping
    @PreAuthorize("hasAuthority('scoring.manage')")
    public ResponseEntity<ApiResponse<PriorityPolicyResponse>> createPolicy(
            @Valid @RequestBody PriorityPolicyRequest request) {
        PriorityPolicyResponse response = policyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Policy created successfully", response));
    }

    @Operation(summary = "Update priority policy")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('scoring.manage')")
    public ResponseEntity<ApiResponse<PriorityPolicyResponse>> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody PriorityPolicyRequest request) {
        PriorityPolicyResponse response = policyService.updatePolicy(id, request);
        return ResponseEntity.ok(ApiResponse.success("Policy updated successfully", response));
    }

    @Operation(summary = "Get priority policy details by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('scoring.policy_view')")
    public ResponseEntity<ApiResponse<PriorityPolicyResponse>> getPolicy(@PathVariable Long id) {
        PriorityPolicyResponse response = policyService.getPolicy(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "List priority policies with optional filters")
    @GetMapping
    @PreAuthorize("hasAuthority('scoring.policy_view')")
    public ResponseEntity<ApiResponse<List<PriorityPolicyListResponse>>> listPolicies(
            @RequestParam(required = false) PolicyEntityType entityType,
            @RequestParam(required = false) PolicyStatus status) {
        List<PriorityPolicyListResponse> list = policyService.listPolicies(entityType, status);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "Activate priority policy (auto-archives the current active policy of same entity type)")
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('scoring.manage')")
    public ResponseEntity<ApiResponse<Void>> activatePolicy(@PathVariable Long id) {
        policyService.activatePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Policy activated successfully", null));
    }

    @Operation(summary = "Archive active priority policy")
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('scoring.manage')")
    public ResponseEntity<ApiResponse<Void>> archivePolicy(@PathVariable Long id) {
        policyService.archivePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Policy archived successfully", null));
    }

    @Operation(summary = "Delete draft priority policy")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('scoring.manage')")
    public ResponseEntity<ApiResponse<Void>> deletePolicy(@PathVariable Long id) {
        policyService.deletePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Policy deleted successfully", null));
    }

    @Operation(summary = "Get available metrics by entity type (for building policy filters/ranking)")
    @GetMapping("/metrics")
    @PreAuthorize("hasAuthority('scoring.policy_view')")
    public ResponseEntity<ApiResponse<List<ComputedMetricResponse>>> getAvailableMetrics(
            @RequestParam PolicyEntityType entityType) {
        List<ComputedMetricResponse> metrics = policyService.getAvailableMetrics(entityType);
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    // ═══════════════════════════════════════════════════════════════════
    // SNAPSHOT SCORING
    // ═══════════════════════════════════════════════════════════════════

    @Operation(summary = "Generate priority snapshot — score all employees in a team against a policy")
    @PostMapping("/{policyId}/snapshots")
    @PreAuthorize("hasAuthority('scoring.manage')")
    public ResponseEntity<ApiResponse<PrioritySnapshotResponse>> generateSnapshot(
            @PathVariable Long policyId,
            @RequestBody @Valid PriorityScoringRequest request) {
        // Override policyId from path
        List<Employee> employees;
        if (request.getEmployeeIds() != null && !request.getEmployeeIds().isEmpty()) {
            employees = employeeRepository.findAllById(request.getEmployeeIds());
        } else {
            employees = employeeRepository.findByTeamsIdAndDeleteFlagFalse(request.getTeamId());
        }

        PrioritySnapshot snapshot = scoringService.generateSnapshot(policyId, request.getTeamId(), employees);
        // Reload with details for response
        PrioritySnapshot fullSnapshot = scoringService.getSnapshotById(snapshot.getId());
        PrioritySnapshotResponse response = snapshotMapper.toResponse(fullSnapshot);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Snapshot generated successfully", response));
    }

    @Operation(summary = "Get snapshot details by ID")
    @GetMapping("/{policyId}/snapshots/{snapshotId}")
    @PreAuthorize("hasAuthority('scoring.view')")
    public ResponseEntity<ApiResponse<PrioritySnapshotResponse>> getSnapshot(
            @PathVariable Long policyId,
            @PathVariable Long snapshotId) {
        PrioritySnapshot snapshot = scoringService.getSnapshotById(snapshotId);
        PrioritySnapshotResponse response = snapshotMapper.toResponse(snapshot);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "List all snapshots for a policy")
    @GetMapping("/{policyId}/snapshots")
    @PreAuthorize("hasAuthority('scoring.view')")
    public ResponseEntity<ApiResponse<List<PrioritySnapshotResponse>>> listSnapshots(
            @PathVariable Long policyId) {
        List<PrioritySnapshot> snapshots = scoringService.listSnapshotsByPolicy(policyId);
        List<PrioritySnapshotResponse> response = snapshots.stream()
                .map(snapshotMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Recalculate priorities — regenerate snapshot with fresh metric data")
    @PostMapping("/{policyId}/snapshots/recalculate")
    @PreAuthorize("hasAuthority('scoring.manage')")
    public ResponseEntity<ApiResponse<PrioritySnapshotResponse>> recalculateSnapshot(
            @PathVariable Long policyId,
            @RequestParam Long teamId) {
        PrioritySnapshot snapshot = scoringService.recalculatePriorities(policyId, teamId);
        PrioritySnapshot fullSnapshot = scoringService.getSnapshotById(snapshot.getId());
        PrioritySnapshotResponse response = snapshotMapper.toResponse(fullSnapshot);
        return ResponseEntity.ok(ApiResponse.success("Priorities recalculated successfully", response));
    }

    @Operation(summary = "Get latest snapshot for a policy + team combination")
    @GetMapping("/{policyId}/snapshots/latest")
    @PreAuthorize("hasAuthority('scoring.view')")
    public ResponseEntity<ApiResponse<PrioritySnapshotResponse>> getLatestSnapshot(
            @PathVariable Long policyId,
            @RequestParam Long teamId) {
        PrioritySnapshot snapshot = scoringService.getLatestSnapshot(policyId, teamId);
        if (snapshot == null) {
            return ResponseEntity.ok(ApiResponse.success("No snapshot found", null));
        }
        PrioritySnapshotResponse response = snapshotMapper.toResponse(snapshot);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Delete a snapshot")
    @DeleteMapping("/{policyId}/snapshots/{snapshotId}")
    @PreAuthorize("hasAuthority('scoring.manage')")
    public ResponseEntity<ApiResponse<Void>> deleteSnapshot(
            @PathVariable Long policyId,
            @PathVariable Long snapshotId) {
        scoringService.deleteSnapshot(snapshotId);
        return ResponseEntity.ok(ApiResponse.success("Snapshot deleted successfully", null));
    }
}
