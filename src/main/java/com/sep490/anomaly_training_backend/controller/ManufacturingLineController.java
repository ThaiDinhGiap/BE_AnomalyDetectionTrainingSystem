package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.EmployeeSkillRequest;
import com.sep490.anomaly_training_backend.dto.request.ProcessRequest;
import com.sep490.anomaly_training_backend.dto.request.ProductLineRequest;
import com.sep490.anomaly_training_backend.dto.request.ProductRequest;
import com.sep490.anomaly_training_backend.dto.response.*;
import com.sep490.anomaly_training_backend.enums.OrgHierarchyLevel;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/manufacturing-lines")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Manufacturing Line Management", description = "API quản lý cấu trúc dữ liệu dây chuyền sản xuất")
public class ManufacturingLineController {
    private final ProductLineService productLineService;
    private final ProcessService processService;
    private final EmployeeSkillService employeeSkillService;
    private final ProductService productService;
    private final ImportHistoryService importHistoryService;

    // For Admin
    @PostMapping("/processes")
    @PreAuthorize("hasAuthority('line_structure.manage')")
    public ResponseEntity<ApiResponse<ProcessResponse>> createProcess(@RequestBody ProcessRequest request) {
        return ResponseEntity.ok(ApiResponse.success(processService.createProcess(request)));
    }

    @PutMapping("/processes/{id}")
    @PreAuthorize("hasAuthority('line_structure.manage')")
    public ResponseEntity<ApiResponse<ProcessResponse>> updateProcess(@PathVariable Long id, @RequestBody ProcessRequest processRequest) {
        return ResponseEntity.ok(ApiResponse.success(processService.updateProcessByAdmin(id, processRequest)));
    }

    @PostMapping("/product-lines")
    @PreAuthorize("hasAuthority('line_structure.manage')")
    public ResponseEntity<ApiResponse<ProductLineResponse>> createProductLine(@RequestBody ProductLineRequest productLineRequest) {
        return ResponseEntity.ok(ApiResponse.success(productLineService.createProductLine(productLineRequest)));
    }

    @PostMapping("/products")
    @PreAuthorize("hasAuthority('product.manage')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@ModelAttribute ProductRequest request, @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(productService.createProduct(request, currentUser)));
    }

    @PostMapping("/products/sync")
    @PreAuthorize("hasAuthority('product.manage')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> syncProduct(@ModelAttribute List<ProductRequest> request, @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(productService.syncProduct(request, currentUser)));
    }

    @PostMapping("/employee-skills")
    @PreAuthorize("hasAuthority('employee_skill.manage')")
    public ResponseEntity<ApiResponse<EmployeeSkillResponse>> createSkill(@RequestBody EmployeeSkillRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeeSkillService.createEmployeeSkill(request)));
    }

    @Operation(summary = "Download products template")
    @GetMapping("/products/template")
    @PreAuthorize("hasAuthority('product.manage')")
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

    @Operation(summary = "Get history import product")
    @GetMapping("/products/import-history")
    @PreAuthorize("hasAuthority('product.manage')")
    public ResponseEntity<ApiResponse<List<ImportHistoryResponse>>> historyProductImport(@AuthenticationPrincipal User currentUser) {
        List<ImportHistoryResponse> responses = importHistoryService.getHistory(currentUser, "PRODUCT_IMPORT");
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/product-lines/{id}")
    @PreAuthorize("hasAuthority('line_structure.manage')")
    public ResponseEntity<ApiResponse<ProductLineResponse>> updateProductLine(@PathVariable Long id, @RequestBody ProductLineRequest productLineRequest) {
        return ResponseEntity.ok(ApiResponse.success(productLineService.updateProductLine(id, productLineRequest)));
    }

    @Operation(summary = "Import Product data")
    @PostMapping("/product-lines/{productLineId}/products/import")
    @PreAuthorize("hasAuthority('product.manage')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> importProduct(@RequestPart("file") MultipartFile file,
                                                                            @AuthenticationPrincipal User currentUser,
                                                                            @PathVariable Long productLineId) throws BadRequestException {
        List<ProductResponse> data = productService.importProduct(currentUser, productLineId, file);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @DeleteMapping("/product-lines/{id}")
    @PreAuthorize("hasAuthority('line_structure.manage')")
    public ResponseEntity<Void> deleteProductLine(@PathVariable Long id) {
        productLineService.deleteProductLine(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/processes/{id}")
    @PreAuthorize("hasAuthority('line_structure.manage')")
    public ResponseEntity<Void> deleteProcess(@PathVariable Long id) {
        processService.deleteProcess(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/product/{id}")
    @PreAuthorize("hasAuthority('line_structure.manage')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/employee-skills/{id}")
    @PreAuthorize("hasAuthority('employee_skill.manage')")
    public ResponseEntity<Void> deleteEmployeeSkill(@PathVariable Long id) {
        employeeSkillService.deleteEmployeeSkill(id);
        return ResponseEntity.noContent().build();
    }


    // For TL/SV/MG
    @GetMapping("/user/working-positions")
    @PreAuthorize("hasAuthority('line_structure.view')")
    public ResponseEntity<ApiResponse<List<WorkingPosition>>> getWorkingPosition(@AuthenticationPrincipal User user) {
        List<WorkingPosition> result = productLineService.getWorkingPosition(user);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/user/org-hierarchy")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Cascading org dropdown",
            description = "Load dropdown items theo level. " +
                    "SECTION → GROUP(sectionId) → TEAM(groupId) → PRODUCT_LINE(groupId)"
    )
    public ResponseEntity<ApiResponse<List<OrgDropdownItem>>> getOrgHierarchy(
            @AuthenticationPrincipal User user,
            @RequestParam OrgHierarchyLevel level,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long groupId) {
        List<OrgDropdownItem> items = productLineService.getOrgHierarchy(user, level, sectionId, groupId);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/product-lines/detail")
    @PreAuthorize("hasAuthority('line_structure.view')")
    public ResponseEntity<ApiResponse<ProductLineResponse>> getProductLineDetail(@RequestParam Long productLineId) {
        return ResponseEntity.ok(ApiResponse.success(productLineService.getProductLineDetail(productLineId)));
    }

    @Operation(summary = "Import Matrix Skill And Product Line Structure")
    @PostMapping("/employee-skills/import")
    @PreAuthorize("hasAuthority('employee_skill.manage')")
    public ResponseEntity<ApiResponse<String>> importMatrixSkill(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser) {

        log.info("User {} is importing skill matrix from file: {}",
                currentUser.getUsername(), file.getOriginalFilename());

        employeeSkillService.importSkillMatrix(file);
        return ResponseEntity.ok(ApiResponse.success("Import skill matrix successfully"));
    }

    @GetMapping("/products/{id}")
    @PreAuthorize("hasAuthority('product.view')")
    public ResponseEntity<ApiResponse<ProductResponse>> findByProductId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    @GetMapping("/product-lines/{id}/full-detail")
    @PreAuthorize("hasAuthority('line_structure.view')")
    @Operation(summary = "Get full detail of a product line", description = "Returns products, processes, and management hierarchy (SV, Section, MNG)")
    public ResponseEntity<ApiResponse<ProductLineDetailResponse>> getProductLineFullDetail(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(productLineService.getProductLineFullDetail(id)));
    }

    @Operation(summary = "Download product lines template")
    @GetMapping("/product-lines/template")
    @PreAuthorize("hasAuthority('line_structure.manage')")
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

    @Operation(summary = "Get history import product")
    @GetMapping("/product-lines/import-history")
    @PreAuthorize("hasAuthority('line_structure.manage')")
    public ResponseEntity<ApiResponse<List<ImportHistoryResponse>>> historyProductLineImport(@AuthenticationPrincipal User currentUser) {
        List<ImportHistoryResponse> responses = importHistoryService.getHistory(currentUser, "PRODUCT_LINE_IMPORT");
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @Operation(summary = "Import ProductLine data")
    @PostMapping("/product-lines/import")
    @PreAuthorize("hasAuthority('line_structure.manage')")
    public ResponseEntity<ApiResponse<List<ProductLineResponse>>> importProductLine(@RequestPart("file") MultipartFile file, @AuthenticationPrincipal User currentUser) throws BadRequestException {
        List<ProductLineResponse> data = productLineService.importProductLine(currentUser, file);
        return ResponseEntity.ok(ApiResponse.success(data));
    }


    //All
    @PutMapping("/employee-skills/{id}")
    @PreAuthorize("hasAuthority('employee_skill.manage')")
    public ResponseEntity<ApiResponse<EmployeeSkillResponse>> updateEmployeeSkill(
            @PathVariable Long id,
            @RequestBody EmployeeSkillRequest employeeSkillRequest) {
        return ResponseEntity.ok(ApiResponse.success(employeeSkillService.updateEmployeeSkillByTeamLead(id, employeeSkillRequest)));
    }

    @GetMapping("/processes/{id}/products")
    @PreAuthorize("hasAuthority('product.view')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductByProcess(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductsByProcessId(id)));
    }

    @GetMapping("/product-lines/{id}/products")
    @PreAuthorize("hasAuthority('product.catalog')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductByProductLine(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductsByProductLineId(id)));
    }

    @GetMapping("/product-lines/{id}/processes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ProcessResponse>>> findProcessByProductLine(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(processService.getProcessesByProductLineId(id)));
    }

    // Unknow
    @GetMapping("/product-lines")
    @PreAuthorize("hasAuthority('line_structure.configure')")
    public ResponseEntity<ApiResponse<List<ProductLineResponse>>> getProductLines() {
        return ResponseEntity.ok(ApiResponse.success(productLineService.getAllProductLine()));
    }

    @GetMapping("/product-lines/by-team-lead")
    @PreAuthorize("hasAuthority('line_structure.view')")
    public ResponseEntity<ApiResponse<List<ProductLineResponse>>> findProductLineByTeamLeadId(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(productLineService.getByTeamLeadId(currentUser.getId())));
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasAuthority('product.manage')")
    public ResponseEntity<ApiResponse<ProcessResponse>> updateProduct() {
        return null;
    }
}
