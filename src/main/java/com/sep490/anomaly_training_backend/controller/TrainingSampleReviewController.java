package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/-review-training-samples")
@RequiredArgsConstructor
@Tag(name = "Training Sample Management", description = "API sắp xếp lịch rà soát danh sách mẫu huấn luyện hàng năm và báo cáo kết quả")
public class TrainingSampleReviewController {

    @Operation(summary = "Lấy kết quả báo cao theo nhóm")
    @GetMapping("/")
    @PreAuthorize("hasAnyAuthority('training_sample_review.view', 'ROLE_SUPERVISOR', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<TrainingSampleResponse>>> getTrainingSampleReviewByProductLine(@RequestParam("productLineId") Long productLineId) {
        return null;
    }
}
