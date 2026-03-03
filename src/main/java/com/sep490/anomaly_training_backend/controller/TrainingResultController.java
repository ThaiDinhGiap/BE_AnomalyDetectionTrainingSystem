package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.FiSignRequest;
import com.sep490.anomaly_training_backend.dto.request.UpdateTrainingResultRequest;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultListResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultOptionResponse;
import com.sep490.anomaly_training_backend.service.TrainingResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/training-result")
@RequiredArgsConstructor
@Tag(name = "Training Result", description = "Quản lý kết quả huấn luyện, chấm điểm và ký xác nhận")
public class TrainingResultController {

    private final TrainingResultService trainingResultService;

    @Operation(summary = "Lấy danh sách nhóm sản phẩm theo Group ID")
    @GetMapping("/product-groups")
    @PreAuthorize("hasAnyAuthority('training_result.view', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SUPERVISOR', 'ROLE_TEAM_LEADER', 'ROLE_FINAL_INSPECTION')")
    public ResponseEntity<List<TrainingResultOptionResponse>> getProductGroups(
            @Parameter(description = "ID của nhóm huấn luyện (Line)") @RequestParam("groupId") Long groupId
    ) {
        return ResponseEntity.ok(trainingResultService.getProductGroupsByLine(groupId));
    }

    @Operation(summary = "Lấy danh sách hạng mục huấn luyện theo Process ID")
    @GetMapping("/topics")
    @PreAuthorize("hasAnyAuthority('training_result.view', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SUPERVISOR', 'ROLE_TEAM_LEADER', 'ROLE_FINAL_INSPECTION')")
    public ResponseEntity<List<TrainingResultOptionResponse>> getTrainingTopics(
            @Parameter(description = "ID của quy trình (Process)") @RequestParam("processId") Long processId
    ) {
        return ResponseEntity.ok(trainingResultService.getTrainingTopicsByProcess(processId));
    }

    @Operation(
            summary = "Cập nhật kết quả huấn luyện (PRO/TL)",
            description = "Cập nhật giờ In/Out, Note. Hệ thống sẽ tự động tính toán Pass/Fail dựa trên Standard Time và tự động điền Actual Date nếu đủ 4 chữ ký."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc lỗi logic tính toán")
    })
    @PutMapping("/update")
    @PreAuthorize("hasAnyAuthority('training_result.edit', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<?> updateTrainingResult(
            @RequestBody UpdateTrainingResultRequest request
    ) {
        try {
            trainingResultService.updateResult(request);
            return ResponseEntity.ok("Cập nhật Training Result thành công.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    @Operation(
            summary = "FI ký xác nhận hàng loạt",
            description = "Dành riêng cho vai trò FINAL_INSPECTION. Nếu sau khi ký đủ 4 chữ ký, Actual Date của Plan và Result sẽ tự động cập nhật."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "FI đã ký thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền ký (Phải là role FINAL_INSPECTION)"),
            @ApiResponse(responseCode = "400", description = "Lỗi dữ liệu đầu vào")
    })
    @PutMapping("/fi-sign")
    @PreAuthorize("hasAnyAuthority('training_result.fi_sign', 'ROLE_FINAL_INSPECTION')")
    public ResponseEntity<?> signByFi(@RequestBody List<FiSignRequest> requests) {
        try {
            trainingResultService.signDetailsByFi(requests);
            return ResponseEntity.ok("FI đã ký xác nhận thành công.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    @Operation(summary = "Lấy danh sách tất cả bản ghi kết quả (Overview)")
    @GetMapping("/list-all")
    @PreAuthorize("hasAnyAuthority('training_result.view', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SUPERVISOR', 'ROLE_TEAM_LEADER', 'ROLE_FINAL_INSPECTION')")
    public ResponseEntity<List<TrainingResultListResponse>> getAllResults() {
        return ResponseEntity.ok(trainingResultService.getAllTrainingResults());
    }

    @Operation(summary = "Lấy chi tiết kết quả huấn luyện theo ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('training_result.view', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SUPERVISOR', 'ROLE_TEAM_LEADER', 'ROLE_FINAL_INSPECTION')")
    public ResponseEntity<TrainingResultDetailResponse> getResultDetail(
            @Parameter(description = "ID của Training Result Header") @PathVariable Long id
    ) {
        return ResponseEntity.ok(trainingResultService.getTrainingResultDetail(id));
    }
}