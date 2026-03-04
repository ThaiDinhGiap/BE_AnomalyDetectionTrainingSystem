package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.*;
import com.sep490.anomaly_training_backend.dto.response.*;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "API quản lý cấu trúc dữ liệu nhân sự và dây chuyền sản xuất")
public class AdminController {
    private final SectionService sectionService;
    private final GroupService groupService;
    private final TeamService teamService;
    private final EmployeeService employeeService;
    private final ProductLineService productLineService;
    private final UserService userService;
    private final ProcessService processService;
    private final EmployeeSkillService employeeSkillService;


    @GetMapping("/sections")
    @PreAuthorize("hasAuthority('admin.view')")
    public ResponseEntity<ApiResponse<List<SectionResponse>>> getSections() {
        return ResponseEntity.ok(ApiResponse.success(sectionService.getAllSections()));
    }

    @GetMapping("/group/{sectionId}")
    @PreAuthorize("hasAnyAuthority('admin.view','ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getGroupsBySection(@PathVariable Long sectionId) {
        return ResponseEntity.ok(ApiResponse.success(groupService.getGroupsBySection(sectionId)));
    }

    @GetMapping("/team/{groupId}")
    @PreAuthorize("hasAnyAuthority('admin.view','ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getTeamsByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getTeamsByGroup(groupId)));
    }

    @GetMapping("/member/{teamId}")
    @PreAuthorize("hasAnyAuthority('admin.view','ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployeesByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeesByTeam(teamId)));
    }

    @GetMapping("/product-lines")
    @PreAuthorize("hasAnyAuthority('admin.view','ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductLineResponse>>> getProductLines() {
        return ResponseEntity.ok(ApiResponse.success(productLineService.getAllProductLine()));
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyAuthority('admin.view','ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDashboard>>> getUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUserDashboard()));
    }

    @GetMapping("/employees")
    @PreAuthorize("hasAnyAuthority('admin.view','ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployees() {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getAllEmployees()));
    }

    // ====================== CREATE ======================

    @PostMapping("/sections")
    @PreAuthorize("hasAnyAuthority('admin.create','ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<SectionResponse>> createSection(@RequestBody SectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sectionService.createSection(request)));
    }

    @PostMapping("/groups")
    @PreAuthorize("hasAnyAuthority('admin.create','ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(@RequestBody GroupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(groupService.createGroup(request)));
    }

    @PostMapping("/teams")
    @PreAuthorize("hasAnyAuthority('admin.create','ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(@RequestBody TeamRequest request) {
        return ResponseEntity.ok(ApiResponse.success(teamService.createTeam(request)));
    }

    @PostMapping("/employees")
    @PreAuthorize("hasAnyAuthority('admin.create','ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(@RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.createEmployee(request)));
    }

    @PostMapping("/product-lines")
    @PreAuthorize("hasAnyAuthority('admin.create','ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ProductLineResponse>> createProductLine(@RequestBody ProductLineRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productLineService.createProductLine(request)));
    }

    @PostMapping("/processes")
    @PreAuthorize("hasAnyAuthority('admin.create','ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ProcessResponse>> createProcess(@RequestBody ProcessRequest request) {
        return ResponseEntity.ok(ApiResponse.success(processService.createProcess(request)));
    }

    @PostMapping("/employee-skills")
    @PreAuthorize("hasAnyAuthority('admin.create','ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeSkillResponse>> createSkill(@RequestBody EmployeeSkillRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeeSkillService.createEmployeeSkill(request)));
    }

    // ====================== UPDATE ======================

    @PutMapping("/sections/{id}")
    @PreAuthorize("hasAnyAuthority('admin.edit','ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<SectionResponse>> updateSection(
            @PathVariable Long id,
            @RequestBody SectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sectionService.updateSection(id, request)));
    }

    @PutMapping("/groups/{id}")
    @PreAuthorize("hasAnyAuthority('admin.edit','ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<GroupResponse>> updateGroup(
            @PathVariable Long id,
            @RequestBody GroupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(groupService.updateGroup(id, request)));
    }

    @PutMapping("/teams/{id}")
    @PreAuthorize("hasAnyAuthority('admin.edit','ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<TeamResponse>> updateTeam(
            @PathVariable Long id,
            @RequestBody TeamRequest request) {
        return ResponseEntity.ok(ApiResponse.success(teamService.updateTeam(id, request)));
    }

    @PutMapping("/employees/{id}")
    @PreAuthorize("hasAnyAuthority('admin.edit','ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long id,
            @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.updateEmployee(id, request)));
    }
    // ====================== DELETE ======================

    @DeleteMapping("/sections/{id}")
    @PreAuthorize("hasAuthority('admin.delete')")
    public ResponseEntity<Void> deleteSection(@PathVariable Long id) {
        sectionService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/groups/{id}")
    @PreAuthorize("hasAuthority('admin.delete')")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/teams/{id}")
    @PreAuthorize("hasAuthority('admin.delete')")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/employees/{id}")
    @PreAuthorize("hasAuthority('admin.delete')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/product-lines/{id}")
    @PreAuthorize("hasAuthority('admin.delete')")
    public ResponseEntity<Void> deleteProductLine(@PathVariable Long id) {
        productLineService.deleteProductLine(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/processes/{id}")
    @PreAuthorize("hasAuthority('admin.delete')")
    public ResponseEntity<Void> deleteProcess(@PathVariable Long id) {
        processService.deleteProcess(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/employee-skills/{id}")
    @PreAuthorize("hasAuthority('admin.delete')")
    public ResponseEntity<Void> deleteEmployeeSkill(@PathVariable Long id) {
        employeeSkillService.deleteEmployeeSkill(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('admin.delete')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}
