package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.scoring.ComputedMetricResponse;
import com.sep490.anomaly_training_backend.dto.scoring.PriorityPolicyListResponse;
import com.sep490.anomaly_training_backend.dto.scoring.PriorityPolicyRequest;
import com.sep490.anomaly_training_backend.dto.scoring.PriorityPolicyResponse;
import com.sep490.anomaly_training_backend.enums.PolicyEntityType;
import com.sep490.anomaly_training_backend.enums.PolicyStatus;
import com.sep490.anomaly_training_backend.service.scoring.PriorityPolicyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@Tag(name = "Priority Policy Management", description = "API quản lý chính sách ưu tiên huấn luyện")
public class PriorityPolicyController {

    private final PriorityPolicyService policyService;

    @PostMapping("/")
    public ResponseEntity<ApiResponse<PriorityPolicyResponse>> createPolicy(
            @Valid @RequestBody PriorityPolicyRequest request) {
        PriorityPolicyResponse response = policyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo chính sách thành công", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PriorityPolicyResponse>> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody PriorityPolicyRequest request) {
        PriorityPolicyResponse response = policyService.updatePolicy(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật chính sách thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PriorityPolicyResponse>> getPolicy(@PathVariable Long id) {
        PriorityPolicyResponse response = policyService.getPolicy(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Page<PriorityPolicyListResponse>>> listPolicies(
            @RequestParam(required = false) PolicyEntityType entityType,
            @RequestParam(required = false) PolicyStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<PriorityPolicyListResponse> page = policyService.listPolicies(entityType, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activatePolicy(@PathVariable Long id) {
        policyService.activatePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Kích hoạt chính sách thành công", null));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<Void>> archivePolicy(@PathVariable Long id) {
        policyService.archivePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Lưu trữ chính sách thành công", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePolicy(@PathVariable Long id) {
        policyService.deletePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa chính sách thành công", null));
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<List<ComputedMetricResponse>>> getAvailableMetrics(
            @RequestParam PolicyEntityType entityType) {
        List<ComputedMetricResponse> metrics = policyService.getAvailableMetrics(entityType);
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }
}
