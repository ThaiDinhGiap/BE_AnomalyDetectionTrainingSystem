package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.scoring.ComputedMetricResponse;
import com.sep490.anomaly_training_backend.dto.scoring.PriorityPolicyListResponse;
import com.sep490.anomaly_training_backend.dto.scoring.PriorityPolicyRequest;
import com.sep490.anomaly_training_backend.dto.scoring.PriorityPolicyResponse;
import com.sep490.anomaly_training_backend.enums.PolicyEntityType;
import com.sep490.anomaly_training_backend.enums.PolicyStatus;
import com.sep490.anomaly_training_backend.service.scoring.PriorityPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/priority-policies")
@RequiredArgsConstructor
@Tag(name = "Priority Policy Management", description = "API for managing training priority policies")
public class PriorityPolicyController {

    private final PriorityPolicyService policyService;

    @Operation(summary = "Create new priority policy")
    @PostMapping("/")
    @PreAuthorize("hasAuthority('policy.create')")
    public ResponseEntity<ApiResponse<PriorityPolicyResponse>> createPolicy(
            @Valid @RequestBody PriorityPolicyRequest request) {
        PriorityPolicyResponse response = policyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Policy created successfully", response));
    }

    @Operation(summary = "Update priority policy")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('policy.edit')")
    public ResponseEntity<ApiResponse<PriorityPolicyResponse>> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody PriorityPolicyRequest request) {
        PriorityPolicyResponse response = policyService.updatePolicy(id, request);
        return ResponseEntity.ok(ApiResponse.success("Policy updated successfully", response));
    }

    @Operation(summary = "Get priority policy details by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('policy.view')")
    public ResponseEntity<ApiResponse<PriorityPolicyResponse>> getPolicy(@PathVariable Long id) {
        PriorityPolicyResponse response = policyService.getPolicy(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get priority policies list (paginated)")
    @GetMapping("/")
    @PreAuthorize("hasAuthority('policy.view')")
    public ResponseEntity<ApiResponse<Page<PriorityPolicyListResponse>>> listPolicies(
            @RequestParam(required = false) PolicyEntityType entityType,
            @RequestParam(required = false) PolicyStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<PriorityPolicyListResponse> page = policyService.listPolicies(entityType, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @Operation(summary = "Activate priority policy")
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('policy.activate')")
    public ResponseEntity<ApiResponse<Void>> activatePolicy(@PathVariable Long id) {
        policyService.activatePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Policy activated successfully", null));
    }

    @Operation(summary = "Archive priority policy")
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('policy.archive')")
    public ResponseEntity<ApiResponse<Void>> archivePolicy(@PathVariable Long id) {
        policyService.archivePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Policy archived successfully", null));
    }

    @Operation(summary = "Delete priority policy")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('policy.delete')")
    public ResponseEntity<ApiResponse<Void>> deletePolicy(@PathVariable Long id) {
        policyService.deletePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Policy deleted successfully", null));
    }

    @Operation(summary = "Get available metrics by entity type")
    @GetMapping("/metrics")
    @PreAuthorize("hasAuthority('policy.view')")
    public ResponseEntity<ApiResponse<List<ComputedMetricResponse>>> getAvailableMetrics(
            @RequestParam PolicyEntityType entityType) {
        List<ComputedMetricResponse> metrics = policyService.getAvailableMetrics(entityType);
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }
}
