package com.sep490.anomaly_training_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleReviewRequest;
import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleReviewConfigResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleReviewResponse;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.TrainingSampleReviewConfigService;
import com.sep490.anomaly_training_backend.service.TrainingSampleReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/-review-training-samples")
@RequiredArgsConstructor
@Tag(name = "Training Sample Management", description = "API sắp xếp lịch rà soát danh sách mẫu huấn luyện hàng năm và báo cáo kết quả")
public class TrainingSampleReviewController {
    private final TrainingSampleReviewConfigService trainingSampleReviewConfigService;
    private final TrainingSampleReviewService trainingSampleReviewService;

    @Operation(summary = "Lấy danh sách cấu hình lịch sắp xếp hàng năm")
    @GetMapping("/config/{productLineId}")
    @PreAuthorize("hasAuthority('training_sample_review.view')")
    public ResponseEntity<List<TrainingSampleReviewConfigResponse>> getTrainingSampleReviewConfigByProductLine(@PathVariable("productLineId") Long productLineId) {
        List<TrainingSampleReviewConfigResponse> response = trainingSampleReviewConfigService.getTrainingSampleReviewConfigByProductLine(productLineId);
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "Lấy danh sách kết quả rà soát hàng năm")
    @GetMapping("/report/{productLineId}")
    @PreAuthorize("hasAuthority('training_sample_review.view')")
    public ResponseEntity<List<TrainingSampleReviewResponse>> getTrainingSampleReviewByProductLine(@PathVariable("productLineId") Long productLineId) {
        List<TrainingSampleReviewResponse> response = trainingSampleReviewService.getTrainingSampleReviewByProductLine(productLineId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Xác nhận hoàn thành rà soát mẫu huấn luyện hàng năm")
    @GetMapping("/confirm")
    @PreAuthorize("hasAuthority('training_sample_review.edit')")
    public ResponseEntity<TrainingSampleReviewResponse> confirmCompleteReviewTrainingSample(@AuthenticationPrincipal User currentUser,
                                                                                            @RequestBody TrainingSampleReviewRequest request) {
        TrainingSampleReviewResponse response = trainingSampleReviewService.confirmReview(currentUser, request);
        return ResponseEntity.ok(response);
    }
}
