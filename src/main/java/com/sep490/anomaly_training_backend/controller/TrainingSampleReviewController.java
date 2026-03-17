package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.TrainingSampleReviewPolicyRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleReviewRequest;
import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleReviewPolicyResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleReviewResponse;
import com.sep490.anomaly_training_backend.service.TrainingSampleReviewPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/review-training-samples")
@RequiredArgsConstructor
@Tag(name = "Training Sample Review Management", description = "API sắp xếp lịch rà soát danh sách mẫu huấn luyện hàng năm và báo cáo kết quả")
public class TrainingSampleReviewController {
    private final TrainingSampleReviewPolicyService trainingSampleReviewPolicyService;
    
    @GetMapping("/policies/product-line/{productLineId}")
    @PreAuthorize("hasAuthority('training_sample_proposal.approve')")
    @Operation(summary = "Get review policies by product line", description = "Lấy danh sách chính sách rà soát theo dây chuyền sản xuất")
    public ResponseEntity<ApiResponse<List<TrainingSampleReviewPolicyResponse>>> getReviewPoliciesByProductLine(
            @PathVariable Long productLineId) {
        List<TrainingSampleReviewPolicyResponse> policies = trainingSampleReviewPolicyService
                .getTrainingSampleReviewPoliciesByProductLine(productLineId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chính sách rà soát thành công", policies));
    }


    @PostMapping("/policies")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEAM_LEAD')")
    @Operation(summary = "Create new review policy", description = "Tạo chính sách rà soát danh sách mẫu huấn luyện mới")
    public ResponseEntity<ApiResponse<TrainingSampleReviewPolicyResponse>> createNewReviewPolicy(
            @RequestBody TrainingSampleReviewPolicyRequest request) {
        TrainingSampleReviewPolicyResponse response = trainingSampleReviewPolicyService.createNewReviewPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo chính sách rà soát thành công", response));
    }


    @GetMapping("/reviews/config/{configId}")
    @PreAuthorize("hasAuthority('training_sample_proposal.approve')")
    @Operation(summary = "Get reviews by config", description = "Lấy danh sách rà soát mẫu huấn luyện theo cấu hình")
    public ResponseEntity<ApiResponse<List<TrainingSampleReviewResponse>>> getReviewsByConfigId(
            @PathVariable Long configId) {
        List<TrainingSampleReviewResponse> reviews = trainingSampleReviewPolicyService.findByConfigId(configId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách rà soát thành công", reviews));
    }


    @GetMapping("/reviews/product-line/{productLineId}")
    @PreAuthorize("hasAuthority('training_sample_proposal.approve')")
    @Operation(summary = "Get reviews by product line", description = "Lấy danh sách rà soát mẫu huấn luyện theo dây chuyền sản xuất")
    public ResponseEntity<ApiResponse<List<TrainingSampleReviewResponse>>> getReviewsByProductLine(
            @PathVariable Long productLineId) {
        List<TrainingSampleReviewResponse> reviews = trainingSampleReviewPolicyService.findByProductLine(productLineId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách rà soát thành công", reviews));
    }


    @DeleteMapping("/policies/{policyId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Delete review policy", description = "Xóa chính sách rà soát (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteReviewPolicy(
            @PathVariable Long policyId) {
        trainingSampleReviewPolicyService.deletePolicy(policyId);
        return ResponseEntity.ok(ApiResponse.success("Xóa chính sách rà soát thành công", null));
    }


    @PostMapping("/reviews/{reviewId}/assign-team-lead")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Assign team lead to review", description = "Gán trưởng nhóm để thực hiện rà soát mẫu huấn luyện")
    public ResponseEntity<ApiResponse<TrainingSampleReviewResponse>> assignTeamLeadToReview(
            @PathVariable Long reviewId,
            @RequestBody TrainingSampleReviewRequest request) {
        request.setId(reviewId);
        TrainingSampleReviewResponse response = trainingSampleReviewPolicyService.assignTeamLeadToReview(request);
        return ResponseEntity.ok(ApiResponse.success("Gán trưởng nhóm thành công", response));
    }


    @PostMapping("/reviews/{reviewId}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEAM_LEAD')")
    @Operation(summary = "Confirm review by team lead", description = "Xác nhận kết quả rà soát mẫu huấn luyện")
    public ResponseEntity<ApiResponse<TrainingSampleReviewResponse>> confirmReviewByTeamLead(
            @PathVariable Long reviewId,
            @RequestBody TrainingSampleReviewRequest request) {
        request.setId(reviewId);
        TrainingSampleReviewResponse response = trainingSampleReviewPolicyService.confirmReviewByTeamLead(request);
        return ResponseEntity.ok(ApiResponse.success("Xác nhận kết quả rà soát thành công", response));
    }
}
