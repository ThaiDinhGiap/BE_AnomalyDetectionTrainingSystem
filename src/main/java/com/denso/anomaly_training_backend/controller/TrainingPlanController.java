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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/training-plans")
@RequiredArgsConstructor
@Tag(name = "01. Training Plan Creation", description = "API quản lý quy trình Tạo mới, Lưu nháp và Gửi duyệt Kế hoạch đào tạo")
public class TrainingPlanController {

    private final TrainingPlanService trainingPlanService;

    // =========================================================================
    // 1. LẤY DỮ LIỆU KHỞI TẠO (Cho màn hình tạo mới)
    // =========================================================================
    @Operation(
            summary = "Lấy dữ liệu khởi tạo cho màn hình Tạo kế hoạch",
            description = "API này trả về danh sách Nhân viên (Employee) và Quy trình (Process) thuộc Group để Frontend hiển thị lên bảng Matrix."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lấy dữ liệu thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingPlanInitDataResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy Group ID", content = @Content)
    })
    @GetMapping("/init-data/{groupId}")
    public ResponseEntity<TrainingPlanInitDataResponse> getInitializationData(
            @Parameter(description = "ID của nhóm (Group) cần lấy dữ liệu", required = true, example = "1")
            @PathVariable Long groupId) {
        return ResponseEntity.ok(trainingPlanService.getInitializationData(groupId));
    }

    // =========================================================================
    // 2. LƯU NHÁP (SAVE DRAFT)
    // =========================================================================
    @Operation(
            summary = "Lưu nháp kế hoạch (Save Draft)",
            description = "Lưu tạm thông tin kế hoạch xuống DB với trạng thái 'DRAFT'. Chưa yêu cầu validate các trường bắt buộc (như Supervisor)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Tạo bản nháp thành công. Trả về ID của kế hoạch vừa tạo.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class, example = "101"))
            ),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ", content = @Content)
    })
    @PostMapping("/draft")
    public ResponseEntity<Long> saveDraft(@RequestBody TrainingPlanRequest request) {
        Long planId = trainingPlanService.saveDraft(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(planId);
    }

    // =========================================================================
    // 3. GỬI DUYỆT (SUBMIT)
    // =========================================================================
    @Operation(
            summary = "Gửi duyệt kế hoạch (Submit)",
            description = "Gửi kế hoạch lên Supervisor. Yêu cầu validate đầy đủ dữ liệu (phải chọn Supervisor). Trạng thái sẽ chuyển sang 'WAITING_SV'."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Gửi duyệt thành công. Trả về ID của kế hoạch.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class, example = "101"))
            ),
            @ApiResponse(responseCode = "400", description = "Thiếu thông tin bắt buộc (VD: chưa chọn Supervisor)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy dữ liệu liên quan (User, Process...)", content = @Content)
    })
    @PostMapping("/submit")
    public ResponseEntity<Long> submitPlan(@RequestBody TrainingPlanRequest request) {
        Long planId = trainingPlanService.submitPlan(request);
        return ResponseEntity.ok(planId);
    }

    // =========================================================================
    // 4. XEM CHI TIẾT (GET DETAIL)
    // =========================================================================
    @Operation(
            summary = "Xem chi tiết kế hoạch đào tạo",
            description = "Lấy toàn bộ thông tin của một kế hoạch, bao gồm cả danh sách ma trận đào tạo và lịch sử duyệt (Approval Logs)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lấy thông tin thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingPlanResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy kế hoạch với ID cung cấp", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<TrainingPlanResponse> getTrainingPlanById(
            @Parameter(description = "ID của kế hoạch đào tạo", required = true, example = "101")
            @PathVariable Long id) {
        TrainingPlanResponse response = trainingPlanService.getTrainingPlanById(id);
        return ResponseEntity.ok(response);
    }
}