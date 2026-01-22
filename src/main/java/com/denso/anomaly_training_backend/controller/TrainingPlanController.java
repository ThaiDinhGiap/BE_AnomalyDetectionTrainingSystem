package com.denso.anomaly_training_backend.controller;

import com.denso.anomaly_training_backend.dto.request.TrainingPlanRequest;
import com.denso.anomaly_training_backend.dto.response.TrainingPlanInitDataResponse;
import com.denso.anomaly_training_backend.dto.response.TrainingPlanResponse;
import com.denso.anomaly_training_backend.service.TrainingPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/training-plans")
@RequiredArgsConstructor
@Tag(name = "01. Training Plan Creation", description = "API liên quan đến việc Tạo mới và Lập kế hoạch")
public class TrainingPlanController {
    private final TrainingPlanService trainingPlanService;

    // =========================================================================
    // 1. LẤY DỮ LIỆU KHỞI TẠO (Cho màn hình tạo mới)
    // =========================================================================
    @GetMapping("/init-data/{groupId}")
    public ResponseEntity<TrainingPlanInitDataResponse> getInitializationData(@PathVariable Long groupId) {
        // Trả về danh sách Employee, Process để FE vẽ bảng Matrix
        return ResponseEntity.ok(trainingPlanService.getInitializationData(groupId));
    }

    // =========================================================================
    // 2. LƯU NHÁP (SAVE DRAFT)
    // =========================================================================
    // FE gọi API này khi user bấm nút "Lưu nháp"
    // Không bắt buộc chọn Supervisor, Status = DRAFT
    @PostMapping("/draft")
    public ResponseEntity<Long> saveDraft(@RequestBody TrainingPlanRequest request) {
        Long planId = trainingPlanService.saveDraft(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(planId);
    }

    // =========================================================================
    // 3. GỬI DUYỆT (SUBMIT)
    // =========================================================================
    // FE gọi API này khi user bấm nút "Gửi duyệt"
    // Bắt buộc chọn Supervisor, Status -> WAITING_SV, Ghi Log
    @PostMapping("/submit")
    public ResponseEntity<Long> submitPlan(@RequestBody TrainingPlanRequest request) {
        Long planId = trainingPlanService.submitPlan(request);
        return ResponseEntity.ok(planId);
    }

    // =========================================================================
    // 4. XEM CHI TIẾT (GET DETAIL)
    // =========================================================================
    // Trả về toàn bộ thông tin + Lịch sử duyệt (Approval Logs)
    @GetMapping("/{id}")
    public ResponseEntity<TrainingPlanResponse> getTrainingPlanById(@PathVariable Long id) {
        TrainingPlanResponse response = trainingPlanService.getTrainingPlanById(id);
        return ResponseEntity.ok(response);
    }
}