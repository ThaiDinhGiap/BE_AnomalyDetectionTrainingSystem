package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.TrainingPlanCreateRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanUpdateRequest;
import com.sep490.anomaly_training_backend.dto.response.GroupResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanResponse;
import com.sep490.anomaly_training_backend.service.TrainingPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/training-plans")
@RequiredArgsConstructor
@Tag(name = "01. Training Plan Management", description = "API quản lý quy trình Tạo mới, Cập nhật và Phê duyệt Kế hoạch đào tạo")
public class TrainingPlanController {

    private final TrainingPlanService trainingPlanService;

    @Operation(summary = "Tạo mới kế hoạch huấn luyện", description = "Tạo kế hoạch ở trạng thái NHÁP (DRAFT).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ")
    })
    @PostMapping
    public ResponseEntity<TrainingPlanResponse> createPlan(@Valid @RequestBody TrainingPlanCreateRequest request) {
        TrainingPlanResponse response = trainingPlanService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Lấy chi tiết kế hoạch theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<TrainingPlanResponse> getPlanDetail(
            @Parameter(description = "ID của kế hoạch") @PathVariable Long id) {
        return ResponseEntity.ok(trainingPlanService.getPlanDetail(id));
    }

    @Operation(summary = "Lấy danh sách tất cả kế hoạch huấn luyện")
    @GetMapping
    public ResponseEntity<List<TrainingPlanResponse>> getAllPlans() {
        return ResponseEntity.ok(trainingPlanService.getAllPlans());
    }

    @Operation(summary = "Lấy danh sách các nhóm (Line) do User hiện tại quản lý")
    @GetMapping("/my-managed-groups")
    public ResponseEntity<List<GroupResponse>> getMyGroups() {
        return ResponseEntity.ok(trainingPlanService.getMyManagedGroups());
    }

    @Operation(
            summary = "Cập nhật nội dung kế hoạch",
            description = "Cập nhật danh sách nhân viên, quy trình và ngày dự kiến (Planned Date). " +
                    "LƯU Ý: Nếu dời lịch, các ngày cũ đã qua mà chưa có Actual Date sẽ tự động được hệ thống ghi chú là 'Nghỉ'."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy kế hoạch"),
            @ApiResponse(responseCode = "400", description = "Lỗi logic khi cập nhật lịch trình")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TrainingPlanResponse> updateTrainingPlan(
            @Parameter(description = "ID của kế hoạch cần sửa") @PathVariable Long id,
            @Valid @RequestBody TrainingPlanUpdateRequest request) {

        TrainingPlanResponse response = trainingPlanService.updatePlan(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Gửi duyệt kế hoạch", description = "Chuyển trạng thái kế hoạch từ DRAFT sang SUBMITTED.")
    @PutMapping("/{id}/submit")
    public ResponseEntity<String> submitPlan(@PathVariable Long id) {
        trainingPlanService.submitPlan(id);
        return ResponseEntity.ok("Gửi duyệt kế hoạch thành công!");
    }

    @Operation(summary = "Hoàn duyệt (Chuyển về Nháp)", description = "Chuyển kế hoạch từ trạng thái chờ duyệt về lại trạng thái Nháp để chỉnh sửa.")
    @PutMapping("/{id}/revert-to-draft")
    public ResponseEntity<String> revertToDraft(@PathVariable Long id) {
        trainingPlanService.revertToDraft(id);
        return ResponseEntity.ok("Đã chuyển kế hoạch về trạng thái nháp thành công!");
    }
}