package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.EmployeeSkillRequest;
import com.sep490.anomaly_training_backend.dto.request.ProcessRequest;
import com.sep490.anomaly_training_backend.dto.request.ProductLineRequest;
import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.EmployeeSkillResponse;
import com.sep490.anomaly_training_backend.dto.response.ImportHistoryResponse;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.dto.response.ProductLineResponse;
import com.sep490.anomaly_training_backend.dto.response.ProductResponse;
import com.sep490.anomaly_training_backend.dto.response.WorkingPosition;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/manufacturing-line")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Manufacturing Line Management", description = "API quản lý cấu trúc dữ liệu dây chuyền sản xuất")
public class ManufacturingLineController {
    private final ProductLineService productLineService;
    private final ProcessService processService;
    private final EmployeeSkillService employeeSkillService;
    private final ProductService productService;
    private final ImportHistoryService importHistoryService;

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

    @GetMapping("/product/{id}")
    @PreAuthorize("hasAuthority('manufacturing_line.view')")
    public ResponseEntity<ApiResponse<ProductResponse>> findByProductId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    @GetMapping("/product-by-process/{id}")
    @PreAuthorize("hasAuthority('manufacturing_line.view')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductByProcess(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductsByProcessId(id)));
    }

    @GetMapping("/product-by-line/{id}")
    @PreAuthorize("hasAuthority('manufacturing_line.view')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductByProductLine(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductsByProductLineId(id)));
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
    @PutMapping("/product")
    @PreAuthorize("hasAuthority('manufacturing_line.edit')")
    public ResponseEntity<ApiResponse<ProcessResponse>> updateProduct() {
        return null;
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
    @PostMapping("/import-products/{productLineId}")
    @PreAuthorize("hasAuthority('manufacturing_line.import')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> importProduct(@RequestPart("file") MultipartFile file,
                                                                            @AuthenticationPrincipal User currentUser,
                                                                            @PathVariable Long productLineId) throws BadRequestException {
        List<ProductResponse> data = productService.importProduct(currentUser, productLineId, file);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Import ProductLine data")
    @PostMapping("/import-product-lines")
    @PreAuthorize("hasAuthority('manufacturing_line.import')")
    public ResponseEntity<ApiResponse<List<ProductLineResponse>>> importProductLine(@RequestPart("file") MultipartFile file, @AuthenticationPrincipal User currentUser) throws BadRequestException {
        List<ProductLineResponse> data = productLineService.importProductLine(currentUser, file);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Import Training Sample template")
    @GetMapping("/product/download-template")
    @PreAuthorize("hasAuthority('training_sample.import')")
    public ResponseEntity<Resource> downloadProductTemplate() throws IOException {
        ClassPathResource file = new ClassPathResource("templates/excel/Product_guideline.xlsx");

        if (!file.exists()) {
            throw new FileNotFoundException("Không tìm thấy file template Excel");
        }
        Resource resource = new InputStreamResource(file.getInputStream());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Product_guideline.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .contentLength(file.contentLength())
                .body(resource);
    }

    @Operation(summary = "Import Training Sample template")
    @GetMapping("/product-line/download-template")
    @PreAuthorize("hasAuthority('training_sample.import')")
    public ResponseEntity<Resource> downloadProductLineTemplate() throws IOException {
        ClassPathResource file = new ClassPathResource("templates/excel/ProductLine_guideline.xlsx");

        if (!file.exists()) {
            throw new FileNotFoundException("Không tìm thấy file template Excel");
        }
        Resource resource = new InputStreamResource(file.getInputStream());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ProductLine_guideline.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .contentLength(file.contentLength())
                .body(resource);
    }

    @Operation(summary = "Import Matrix Skill And Product Line Structure")
    @PostMapping("/import-matrix-skill")
    @PreAuthorize("hasAuthority('manufacturing_line.import')")
    public ResponseEntity<ApiResponse<String>> importMatrixSkill(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser) {

        log.info("User {} is importing skill matrix from file: {}",
                currentUser.getUsername(), file.getOriginalFilename());

        employeeSkillService.importSkillMatrix(file);
        return ResponseEntity.ok(ApiResponse.success("Import skill matrix successfully"));
    }

    @Operation(summary = "Get history import Training Sample")
    @GetMapping("/product/import-history")
    @PreAuthorize("hasAuthority('training_sample.import')")
    public ResponseEntity<ApiResponse<List<ImportHistoryResponse>>> historyProductImport(@AuthenticationPrincipal User currentUser) {
        List<ImportHistoryResponse> responses = importHistoryService.getHistory(currentUser, "PRODUCT_IMPORT");
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @Operation(summary = "Get history import Training Sample")
    @GetMapping("/product-line/import-history")
    @PreAuthorize("hasAuthority('training_sample.import')")
    public ResponseEntity<ApiResponse<List<ImportHistoryResponse>>> historyProductLineImport(@AuthenticationPrincipal User currentUser) {
        List<ImportHistoryResponse> responses = importHistoryService.getHistory(currentUser, "PRODUCT_LINE_IMPORT");
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
