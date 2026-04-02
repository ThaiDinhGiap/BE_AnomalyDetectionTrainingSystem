package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.FiSignRequest;
import com.sep490.anomaly_training_backend.dto.request.UpdateTrainingResultRequest;
import com.sep490.anomaly_training_backend.dto.response.EmployeeSkillCertificateResponse;
import com.sep490.anomaly_training_backend.dto.response.KpiSummaryResponse;
import com.sep490.anomaly_training_backend.dto.response.PrioritizedEmployeeResponse;
import com.sep490.anomaly_training_backend.dto.response.ProductLineResponse;
import com.sep490.anomaly_training_backend.dto.response.SampleResultResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultListResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultOptionResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultProcessResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultProductOptionResponse;
import com.sep490.anomaly_training_backend.dto.response.skill_matrix.SkillMatrixResponse;
import com.sep490.anomaly_training_backend.model.TrainingResultDetail;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.TrainingResultDetailRepository;
import com.sep490.anomaly_training_backend.service.EmployeeSkillService;
import com.sep490.anomaly_training_backend.service.TrainingResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/training-results")
@RequiredArgsConstructor
@Tag(name = "Training Result", description = "Manage training results, scoring and signature confirmation")
public class TrainingResultController {

    private final TrainingResultService trainingResultService;
    private final EmployeeSkillService employeeSkillService;
    private final TrainingResultDetailRepository trainingResultDetailRepository;

    @Operation(summary = "[TEST] Query pending details with isPass not null")
    @PreAuthorize("hasAuthority('training_result.view')")
    @GetMapping("/test/pending-with-pass/{resultId}")
    public ResponseEntity<List<Map<String, Object>>> testPendingWithIsPass(
            @PathVariable Long resultId) {
        List<TrainingResultDetail> details = trainingResultDetailRepository
                .findPendingWithIsPassByResultId(resultId);

        List<Map<String, Object>> result = details.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            map.put("trainingResultId", d.getTrainingResult().getId());
            map.put("employeeId", d.getEmployee().getId());
            map.put("status", d.getStatus());
            map.put("isPass", d.getIsPass());
            map.put("plannedDate", d.getPlannedDate());
            map.put("actualDate", d.getActualDate());
            map.put("note", d.getNote());
            map.put("processId", d.getProcess() != null ? d.getProcess().getId() : null);
            return map;
        }).toList();

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get skill matrix data")
    @GetMapping("/skills/matrix")
    @PreAuthorize("hasAuthority('employee_skill.view')")
    public ResponseEntity<SkillMatrixResponse> getSkillMatrix(
            @Parameter(description = "Filter by Team ID", required = true) @RequestParam Long teamId,
            @Parameter(description = "Filter by Product Line ID", required = true) @RequestParam Long lineId,
            @Parameter(description = "Optional list of Employee IDs to display") @RequestParam(required = false) List<Long> employeeIds,
            @Parameter(description = "Optional list of Process IDs to display") @RequestParam(required = false) List<Long> processIds) {
        return ResponseEntity.ok(employeeSkillService.getSkillMatrix(teamId, lineId, employeeIds, processIds));
    }

    @Operation(summary = "Get KPI Summary (Top Cards)")
    @GetMapping("/kpi-summary")
    @PreAuthorize("hasAuthority('training_result.view')")
    public ResponseEntity<KpiSummaryResponse> getKpiSummary(
            @Parameter(description = "Filter by Team ID") @RequestParam(required = false) Long teamId,
            @Parameter(description = "Filter by Line ID") @RequestParam(required = false) Long lineId,
            @Parameter(description = "Filter by Year") @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(trainingResultService.getKpiSummary(teamId, lineId, year));
    }

    @Operation(summary = "Get product groups by Group ID")
    @GetMapping("/product-groups")
    @PreAuthorize("hasAuthority('training_result.view')")
    public ResponseEntity<List<TrainingResultOptionResponse>> getProductGroups(
            @Parameter(description = "Training group (Line) ID") @RequestParam("groupId") Long groupId) {
        return ResponseEntity.ok(trainingResultService.getProductGroupsByLine(groupId));
    }

    @Operation(summary = "Get training samples by product", description = "Lấy danh sách mẫu luyện tập dựa trên sản phẩm đã chọn.")
    @GetMapping("/samples-by-product")
    public ResponseEntity<List<SampleResultResponse>> getSamplesByProduct(
            @Parameter(description = "ID của sản phẩm") @RequestParam("productId") Long productId) {

        return ResponseEntity.ok(trainingResultService.getSamplesByProduct(productId));
    }

    @Operation(summary = "Get training topics by Process ID")
    @GetMapping("/topics")
    @PreAuthorize("hasAuthority('training_result.view')")
    public ResponseEntity<List<TrainingResultOptionResponse>> getTrainingTopics(
            @Parameter(description = "Process ID") @RequestParam("processId") Long processId) {
        return ResponseEntity.ok(trainingResultService.getTrainingTopicsByProcess(processId));
    }

    @Operation(summary = "Update training result (PRO/TL)", description = "Update In/Out times, Notes. System will automatically calculate Pass/Fail based on Standard Time and auto-fill Actual Date when all 4 signatures are complete.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data or calculation logic error")
    })
    @PutMapping
    @PreAuthorize("hasAuthority('training_result.manage')")
    public ResponseEntity<?> updateTrainingResult(
            @RequestBody UpdateTrainingResultRequest request) {
        try {
            trainingResultService.updateResult(request);
            return ResponseEntity.ok("Training Result updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "FI batch signature confirmation", description = "For FINAL_INSPECTION role only. When all 4 signatures are complete, Actual Date of Plan and Result will be automatically updated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "FI signed successfully"),
            @ApiResponse(responseCode = "403", description = "No signing permission (Must be FINAL_INSPECTION role)"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/fi-signatures")
    @PreAuthorize("hasAuthority('review_approve.confirm')")
    public ResponseEntity<?> signByFi(@RequestBody List<FiSignRequest> requests) {
        try {
            trainingResultService.signDetailsByFi(requests);
            return ResponseEntity.ok("FI signature confirmed successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "Submit training result for approval (GỬI KẾT QUẢ)", description = "Submit the training result. All details must have been filled and signed before submission.")
    @PutMapping("/{id}/submit-confirmed-result")
    @PreAuthorize("hasAuthority('review_approve.confirm')")
    public ResponseEntity<String> submitConfirmedResult(
            @Parameter(description = "Training Result ID") @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        trainingResultService.submitConfirmedResult(id, currentUser);
        return ResponseEntity.ok("Kết quả huấn luyện đã được gửi thành công!");
    }

    @Operation(summary = "Get all training result records (Overview)")
    @GetMapping
    @PreAuthorize("hasAuthority('training_result.view')")
    public ResponseEntity<List<TrainingResultListResponse>> getAllResults(
            @Parameter(description = "Filter by Product Line ID") @RequestParam(required = false) Long lineId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(trainingResultService.getAllTrainingResults(currentUser, lineId));
    }

    @Operation(summary = "Get product lines managed by current user", description = "Returns list of product lines that belong to the group of the current Team Leader's team. Used for the line dropdown filter.")
    @GetMapping("/my-lines")
    @PreAuthorize("hasAuthority('training_result.view')")
    public ResponseEntity<List<ProductLineResponse>> getMyProductLines() {
        return ResponseEntity.ok(trainingResultService.getMyProductLines());
    }

    @Operation(summary = "Get all employees in result's team", description = "Returns list of all active employees in the same team as the result.")
    @GetMapping("/{resultId}/employees")
    @PreAuthorize("hasAuthority('training_result.view')")
    public ResponseEntity<List<PrioritizedEmployeeResponse>> getEmployeesInTeam(
            @Parameter(description = "Result ID") @PathVariable Long resultId) {
        return ResponseEntity.ok(trainingResultService.getEmployeesInTeams(resultId));
    }

    @Operation(summary = "Get training results by product line (dây chuyền)", description = "Returns list of training results filtered by the selected product line.")
    @GetMapping("/by-line/{lineId}")
    @PreAuthorize("hasAuthority('training_result.view')")
    public ResponseEntity<List<TrainingResultListResponse>> getResultsByLine(
            @Parameter(description = "Product Line ID") @PathVariable Long lineId) {
        return ResponseEntity.ok(trainingResultService.getResultsByLine(lineId));
    }

    @Operation(summary = "Get training result details by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('training_result.view')")
    public ResponseEntity<TrainingResultDetailResponse> getResultDetail(
            @Parameter(description = "Training Result Header ID") @PathVariable Long id) {
        return ResponseEntity.ok(trainingResultService.getTrainingResultDetail(id));
    }

    @Operation(summary = "Get training result details for Supervisor", description = "Returns only details with status PENDING_REVIEW, REJECTED, or APPROVED.")
    @GetMapping("/{id}/verify-view")
    @PreAuthorize("hasAuthority('training_result.view')")
    public ResponseEntity<TrainingResultDetailResponse> getResultDetailForVerify(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "Training Result Header ID") @PathVariable Long id) {
        return ResponseEntity.ok(trainingResultService.getTrainingResultDetailForVerify(currentUser, id));
    }

    @Operation(summary = "Get processes by product line", description = "Returns list of processes for the Công đoạn dropdown on the result detail screen.")
    @GetMapping("/processes-by-line/{lineId}")
    @PreAuthorize("hasAuthority('training_result.view')")
    public ResponseEntity<List<TrainingResultOptionResponse>> getProcessesByLine(
            @Parameter(description = "Product Line ID") @PathVariable Long lineId) {
        return ResponseEntity.ok(trainingResultService.getProcessesByLine(lineId));
    }

    @Operation(summary = "Get processes by employee skill", description = "Returns list of processes that the employee has skills for, filtered by product line. Used for the Công đoạn dropdown when selecting per employee.")
    @GetMapping("/processes-by-employee")
    @PreAuthorize("hasAuthority('training_result.view')")
    public ResponseEntity<List<TrainingResultProcessResponse>> getProcessesByEmployeeSkill(
            @Parameter(description = "Employee ID") @RequestParam("employeeId") Long employeeId,
            @Parameter(description = "Product Line ID") @RequestParam("lineId") Long lineId) {
        return ResponseEntity.ok(trainingResultService.getProcessesByEmployeeSkill(employeeId, lineId));
    }

    @Operation(summary = "Get products by process", description = "Returns list of products (mã sản phẩm) linked to a specific process via product_process table.")
    @GetMapping("/products-by-process/{processId}")
    @PreAuthorize("hasAuthority('training_result.view')")
    public ResponseEntity<List<TrainingResultProductOptionResponse>> getProductsByProcess(
            @Parameter(description = "Process ID") @PathVariable Long processId) {
        return ResponseEntity.ok(trainingResultService.getProductsByProcess(processId));
    }

    @Operation(summary = "Get products for dropdown", description = "Returns list of all products (mã sản phẩm) for the product dropdown on the result detail screen.")
    @GetMapping("/products")
    @PreAuthorize("hasAuthority('training_result.view')")
    public ResponseEntity<List<TrainingResultOptionResponse>> getProducts() {
        return ResponseEntity.ok(trainingResultService.getProductGroupsByLine(null));
    }

    @Operation(summary = "Submit training result for approval (GỬI KẾT QUẢ)", description = "Submit the training result. All details must have been filled and signed before submission.")
    @PutMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('training_result.manage')")
    public ResponseEntity<String> submitResult(
            @Parameter(description = "Training Result ID") @PathVariable Long id,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        trainingResultService.submit(id, currentUser, request);
        return ResponseEntity.ok("Kết quả huấn luyện đã được gửi thành công!");
    }

    @Operation(summary = "Mark detail for retraining (Huấn luyện lại)", description = "Mark a specific result detail as needing retraining and create a new detail row.")
    @PutMapping("/details/{detailId}/retrain")
    @PreAuthorize("hasAuthority('training_result.manage')")
    public ResponseEntity<String> retrainDetail(
            @Parameter(description = "Detail ID") @PathVariable Long detailId) {
        trainingResultService.retrainDetail(detailId);
        return ResponseEntity.ok("Đã đánh dấu huấn luyện lại!");
    }

    @Operation(summary = "Revise a rejected detail (Chỉnh sửa lại detail bị từ chối)", description = "Chuyển detail bị reject về PENDING và tạo snapshot lịch sử.")
    @PutMapping("/details/{detailId}/revise")
    @PreAuthorize("hasAuthority('training_result.manage')")
    public ResponseEntity<String> reviseDetail(
            @Parameter(description = "Detail ID") @PathVariable Long detailId) {
        trainingResultService.reviseDetail(detailId);
        return ResponseEntity.ok("Đã revise detail thành công!");
    }

    @Operation(summary = "Get skill certificates for training result")
    @GetMapping("/{resultId}/skill-certificates")
    @PreAuthorize("hasAuthority('training_result.view')")
    public ResponseEntity<List<EmployeeSkillCertificateResponse>> getSkillCertificates(
            @PathVariable Long resultId) {
        return ResponseEntity.ok(trainingResultService.getSkillCertificates(resultId));
    }

    @Operation(summary = "Approve training result", description = "Approve the training result. Only authorized personnel can perform this action.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Approved successfully"),
            @ApiResponse(responseCode = "403", description = "No approval permission"),
            @ApiResponse(responseCode = "404", description = "Result not found")
    })
    @PutMapping("/{id}/approve/{detailId}")
    @PreAuthorize("hasAnyAuthority('review_approve.review', 'review_approve.approve')")
    public ResponseEntity<String> approveResultDetail(
            @PathVariable Long id,
            @PathVariable Long detailId,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ApproveRequest approveRequest,
            HttpServletRequest request) {
        trainingResultService.approveDetail(id, detailId, approveRequest, currentUser, request);
        return ResponseEntity.ok("Plan has been approved successfully!");
    }

    @Operation(summary = "Reject training result", description = "Reject and request revision of the result.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Result rejected"),
            @ApiResponse(responseCode = "400", description = "Invalid rejection reason")
    })
    @PutMapping("/{id}/reject/{detailId}")
    @PreAuthorize("hasAnyAuthority('review_approve.review', 'review_approve.approve')")
    public ResponseEntity<String> rejectResultDetail(
            @PathVariable Long id,
            @PathVariable Long detailId,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody RejectRequest detailRejectRequest,
            HttpServletRequest request) {
        trainingResultService.rejectDetail(id, detailId, detailRejectRequest, currentUser, request);
        return ResponseEntity.ok("Plan has been rejected!");
    }
}
