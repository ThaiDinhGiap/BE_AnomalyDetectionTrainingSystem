package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.EmployeeSkillRequest;
import com.sep490.anomaly_training_backend.dto.request.ProcessRequest;
import com.sep490.anomaly_training_backend.dto.request.ProductLineRequest;
import com.sep490.anomaly_training_backend.dto.response.*;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.EmployeeSkillService;
import com.sep490.anomaly_training_backend.service.ProcessService;
import com.sep490.anomaly_training_backend.service.ProductLineService;
import com.sep490.anomaly_training_backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/manufacturing-line")
@RequiredArgsConstructor
@Tag(name = "Manufacturing Line Management", description = "API quản lý cấu trúc dữ liệu dây chuyền sản xuất")
public class ManufacturingLineController {
    private final ProductLineService productLineService;
    private final ProcessService processService;
    private final EmployeeSkillService employeeSkillService;
    private final ProductService productService;

    @GetMapping("/product-lines")
    @PreAuthorize("hasAuthority('manufacturing_line.view')")
    public ResponseEntity<ApiResponse<List<ProductLineResponse>>> getProductLines() {
        return ResponseEntity.ok(ApiResponse.success(productLineService.getAllProductLine()));

    }
    @GetMapping("/user/working-positions")
    @PreAuthorize("hasAuthority('manufacturing_line.view')")
    public ResponseEntity<ApiResponse<List<WorkingPosition>>> getWorkingPosition(@AuthenticationPrincipal User user) {
        List<WorkingPosition> result = productLineService.getWorkingPosition(user);
        return ResponseEntity.ok(ApiResponse.success(result));

    }
    @GetMapping("/product-lines-detail")
    @PreAuthorize("hasAuthority('manufacturing_line.view')")
    public ResponseEntity<ApiResponse<ProductLineResponse>> getProductLineDetail(@RequestParam Long productLineId) {
        return ResponseEntity.ok(ApiResponse.success(productLineService.getProductLineDetail(productLineId)));
    }

    @GetMapping("/lines-by-teamlead")
    @PreAuthorize("hasAuthority('manufacturing_line.view')")
    public ResponseEntity<ApiResponse<List<ProductLineResponse>>> findProductLineByTeamLeadId(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(productLineService.getByTeamLeadId(currentUser.getId())));
    }

    @GetMapping("/processes-by-line/{id}")
    @PreAuthorize("hasAuthority('manufacturing_line.view')")
    public ResponseEntity<ApiResponse<List<ProcessResponse>>> findProcessByProductLine(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(processService.getProcessesByProductLineId(id)));
    }

    @GetMapping("/product-by-process/{id}")
    @PreAuthorize("hasAuthority('manufacturing_line.view')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductByProcess(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductsByProcessId(id)));
    }

    @PostMapping("/product-lines")
    @PreAuthorize("hasAuthority('manufacturing_line.view')")
    public ResponseEntity<ApiResponse<ProductLineResponse>> createProductLineByTeamLead(@RequestBody ProductLineRequest productLineRequest) {
        return ResponseEntity.ok(ApiResponse.success(productLineService.createProductLine(productLineRequest)));
    }

    @PostMapping("/processes")
    @PreAuthorize("hasAuthority('manufacturing_line.create')")
    public ResponseEntity<ApiResponse<ProcessResponse>> createProcess(@RequestBody ProcessRequest request) {
        return ResponseEntity.ok(ApiResponse.success(processService.createProcess(request)));
    }

    @PostMapping("/employee-skills")
    @PreAuthorize("hasAuthority('manufacturing_line.create')")
    public ResponseEntity<ApiResponse<EmployeeSkillResponse>> createSkill(@RequestBody EmployeeSkillRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeeSkillService.createEmployeeSkill(request)));
    }
    // ====================== UPDATE ======================
    @PutMapping("/product-lines/{id}")
    @PreAuthorize("hasAuthority('manufacturing_line.edit')")
    public ResponseEntity<ApiResponse<ProductLineResponse>> updateProductLine(@PathVariable Long id, @RequestBody ProductLineRequest productLineRequest) {
        return ResponseEntity.ok(ApiResponse.success(productLineService.updateProductLine(id, productLineRequest)));
    }
    @PutMapping("/processes/{id}")
    @PreAuthorize("hasAuthority('manufacturing_line.edit')")
    public ResponseEntity<ApiResponse<ProcessResponse>> updateProcess(@PathVariable Long id, @RequestBody ProcessRequest processRequest) {
        return ResponseEntity.ok(ApiResponse.success(processService.updateProcessByAdmin(id, processRequest)));
    }
//    @PutMapping("/employee-skills/{id}")
//    @PreAuthorize("hasAuthority('manufacturing_line.edit')")
//    public ResponseEntity<ApiResponse<EmployeeSkillResponse>> updateEmployeeSkillByTeamLead(@PathVariable Long id, @RequestBody List<EmployeeSkillRequest> employeeSkillRequestList) {
//        return ResponseEntity.ok(ApiResponse.success(employeeSkillService.updateEmployeeSkillByTeamLead(id, employeeSkillRequestList)));
//    }

    // ====================== DELETE ======================
    @DeleteMapping("/product-lines/{id}")
    @PreAuthorize("hasAuthority('manufacturing_line.delete')")
    public ResponseEntity<Void> deleteProductLine(@PathVariable Long id) {
        productLineService.deleteProductLine(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/processes/{id}")
    @PreAuthorize("hasAuthority('manufacturing_line.delete')")
    public ResponseEntity<Void> deleteProcess(@PathVariable Long id) {
        processService.deleteProcess(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/employee-skills/{id}")
    @PreAuthorize("hasAuthority('manufacturing_line.delete')")
    public ResponseEntity<Void> deleteEmployeeSkill(@PathVariable Long id) {
        employeeSkillService.deleteEmployeeSkill(id);
        return ResponseEntity.noContent().build();
    }
    // ====================== IMPORT ======================
    @Operation(summary = "Import Product data")
    @PostMapping("/import-products")
    @PreAuthorize("hasAuthority('manufacturing_line.import')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> importProduct(@RequestPart("file") MultipartFile file, @AuthenticationPrincipal User currentUser) throws BadRequestException {
        List<ProductResponse> data = productService.importProduct(currentUser, file);
        return ResponseEntity.ok(ApiResponse.success( data));
    }
    @Operation(summary = "Import ProductLine data")
    @PostMapping("/import-product-lines")
    @PreAuthorize("hasAuthority('manufacturing_line.import')")
    public ResponseEntity<ApiResponse<List<ProductLineResponse>>> importProductLine(@RequestPart("file") MultipartFile file, @AuthenticationPrincipal User currentUser) throws BadRequestException {
        List<ProductLineResponse> data = productLineService.importProductLine(currentUser, file);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
