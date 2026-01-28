package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.TrainingPlanCreateRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanUpdateRequest;
import com.sep490.anomaly_training_backend.dto.response.GroupResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanResponse;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.TrainingPlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/training-plans")
@RequiredArgsConstructor
@Tag(name = "01. Training Plan Creation", description = "API quản lý quy trình Tạo mới, Lưu nháp và Gửi duyệt Kế hoạch đào tạo")
public class TrainingPlanController {

    private final TrainingPlanService trainingPlanService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<TrainingPlanResponse> createPlan(@Valid @RequestBody TrainingPlanCreateRequest request) {
        TrainingPlanResponse response = trainingPlanService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainingPlanResponse> getPlanDetail(@PathVariable Long id) {
        return ResponseEntity.ok(trainingPlanService.getPlanDetail(id));
    }

    @GetMapping
    public ResponseEntity<List<TrainingPlanResponse>> getAllPlans() {
        return ResponseEntity.ok(trainingPlanService.getAllPlans());
    }

    @GetMapping("/my-managed-groups")
    public ResponseEntity<List<GroupResponse>> getMyGroups() {
        return ResponseEntity.ok(trainingPlanService.getMyManagedGroups());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainingPlanResponse> updateTrainingPlan(
            @PathVariable Long id,
            @Valid @RequestBody TrainingPlanUpdateRequest request) {

        TrainingPlanResponse response = trainingPlanService.updatePlan(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/submit")
    public ResponseEntity<String> submitPlan(@PathVariable Long id) {
        trainingPlanService.submitPlan(id);
        return ResponseEntity.ok("Gửi duyệt kế hoạch thành công!");
    }

    @PutMapping("/{id}/revert-to-draft")
    public ResponseEntity<String> revertToDraft(@PathVariable Long id) {
        trainingPlanService.revertToDraft(id);
        return ResponseEntity.ok("Đã chuyển kế hoạch về trạng thái nháp thành công!");
    }

}