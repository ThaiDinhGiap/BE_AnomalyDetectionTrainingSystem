package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.EmployeeSkillRequest;
import com.sep490.anomaly_training_backend.dto.request.ProcessRequest;
import com.sep490.anomaly_training_backend.dto.request.ProductLineRequest;
import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.EmployeeSkillResponse;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.dto.response.ProductLineResponse;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.EmployeeSkillService;
import com.sep490.anomaly_training_backend.service.ProcessService;
import com.sep490.anomaly_training_backend.service.ProductLineService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/manufacturing-line")
@RequiredArgsConstructor
@Tag(name = "Manufacturing Line Management", description = "API quản lý cấu trúc dữ liệu dây chuyền sản xuất")
public class ManufacturingLineController {
    private final ProductLineService productLineService;
    private final ProcessService processService;
    private final EmployeeSkillService employeeSkillService;

    @GetMapping("/product-lines")
    @PreAuthorize("hasAuthority('manufacturing-line.view')")
    public ResponseEntity<ApiResponse<List<ProductLineResponse>>> getProductLines() {
        return ResponseEntity.ok(ApiResponse.success(productLineService.getAllProductLine()));
    }

    @GetMapping("/lines-by-teamlead")
    @PreAuthorize("hasAuthority('manufacturing-line.view')")
    public ResponseEntity<ApiResponse<List<ProductLineResponse>>> findProductLineByTeamLeadId(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(productLineService.getByTeamLeadId(currentUser.getId())));
    }

    @PostMapping("/product-lines")
    @PreAuthorize("hasAuthority('manufacturing-line.view')")
    public ResponseEntity<ApiResponse<ProductLineResponse>> createProductLineByTeamLead(@RequestBody ProductLineRequest productLineRequest) {
        return ResponseEntity.ok(ApiResponse.success(productLineService.createProductLine(productLineRequest)));
    }

    @PostMapping("/processes")
    @PreAuthorize("hasAuthority('manufacturing-line.create')")
    public ResponseEntity<ApiResponse<ProcessResponse>> createProcess(@RequestBody ProcessRequest request) {
        return ResponseEntity.ok(ApiResponse.success(processService.createProcess(request)));
    }

    @PostMapping("/employee-skills")
    @PreAuthorize("hasAuthority('manufacturing-line.create')")
    public ResponseEntity<ApiResponse<EmployeeSkillResponse>> createSkill(@RequestBody EmployeeSkillRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeeSkillService.createEmployeeSkill(request)));
    }
    // ====================== UPDATE ======================
    @PutMapping("/product-lines/{id}")
    @PreAuthorize("hasAuthority('manufacturing-line.edit')")
    public ResponseEntity<ApiResponse<ProductLineResponse>> updateProductLine(@PathVariable Long id, @RequestBody ProductLineRequest productLineRequest) {
        return ResponseEntity.ok(ApiResponse.success(productLineService.updateProductLine(id, productLineRequest)));
    }
    @PutMapping("/processes/{id}")
    @PreAuthorize("hasAuthority('manufacturing-line.edit')")
    public ResponseEntity<ApiResponse<ProcessResponse>> updateProcess(@PathVariable Long id, @RequestBody ProcessRequest processRequest) {
        return ResponseEntity.ok(ApiResponse.success(processService.updateProcessByAdmin(id, processRequest)));
    }
//    @PutMapping("/employee-skills/{id}")
//    @PreAuthorize("hasAuthority('manufacturing-line.edit')")
//    public ResponseEntity<ApiResponse<EmployeeSkillResponse>> updateEmployeeSkillByTeamLead(@PathVariable Long id, @RequestBody List<EmployeeSkillRequest> employeeSkillRequestList) {
//        return ResponseEntity.ok(ApiResponse.success(employeeSkillService.updateEmployeeSkillByTeamLead(id, employeeSkillRequestList)));
//    }

    // ====================== DELETE ======================
    @DeleteMapping("/product-lines/{id}")
    @PreAuthorize("hasAuthority('manufacturing-line.delete')")
    public ResponseEntity<Void> deleteProductLine(@PathVariable Long id) {
        productLineService.deleteProductLine(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/processes/{id}")
    @PreAuthorize("hasAuthority('manufacturing-line.delete')")
    public ResponseEntity<Void> deleteProcess(@PathVariable Long id) {
        processService.deleteProcess(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/employee-skills/{id}")
    @PreAuthorize("hasAuthority('manufacturing-line.delete')")
    public ResponseEntity<Void> deleteEmployeeSkill(@PathVariable Long id) {
        employeeSkillService.deleteEmployeeSkill(id);
        return ResponseEntity.noContent().build();
    }
}
