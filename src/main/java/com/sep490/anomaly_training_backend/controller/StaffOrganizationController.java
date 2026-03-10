package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.*;
import com.sep490.anomaly_training_backend.dto.response.*;
import com.sep490.anomaly_training_backend.service.*;
import com.sep490.anomaly_training_backend.service.impl.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff-organization")
@RequiredArgsConstructor
@Tag(name = "Staff Organization API", description = "Quản lý cơ cấu tổ chức nhân sự (Phòng ban, Nhóm, Tổ, Nhân viên, Người dùng)")
public class StaffOrganizationController {

    private final SectionService sectionService;
    private final GroupService groupService;
    private final TeamService teamService;
    private final EmployeeService employeeService;
    private final UserService userService;
    private final AuthService authService;

    // ====================== VIEW ======================

    @GetMapping("/sections")
    @PreAuthorize("hasAuthority('staff_organization.view')")
    @Operation(summary = "Lấy danh sách Phòng/Ban (Sections)", description = "Trả về danh sách tất cả các phòng ban trong hệ thống")
    public ResponseEntity<ApiResponse<List<SectionResponse>>> getSections() {
        return ResponseEntity.ok(ApiResponse.success(sectionService.getAllSections()));
    }

    @GetMapping("/group/{sectionId}")
    @PreAuthorize("hasAuthority('staff_organization.view')")
    @Operation(summary = "Lấy danh sách Nhóm (Groups) theo Section ID", description = "Truyền vào ID của phòng ban để lấy danh sách các nhóm thuộc phòng ban đó")
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getGroupsBySection(
            @Parameter(description = "ID của Section (Phòng/Ban)") @PathVariable Long sectionId) {
        return ResponseEntity.ok(ApiResponse.success(groupService.getGroupsBySection(sectionId)));
    }

    @GetMapping("/team/{groupId}")
    @PreAuthorize("hasAuthority('staff_organization.view')")
    @Operation(summary = "Lấy danh sách Tổ (Teams) theo Group ID", description = "Truyền vào ID của nhóm để lấy danh sách các tổ thuộc nhóm đó")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getTeamsByGroup(
            @Parameter(description = "ID của Group (Nhóm)") @PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getTeamsByGroup(groupId)));
    }

    @GetMapping("/member/{teamId}")
    @PreAuthorize("hasAuthority('staff_organization.view')")
    @Operation(summary = "Lấy danh sách Nhân viên theo Team ID", description = "Truyền vào ID của tổ để lấy danh sách nhân viên thuộc tổ đó")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployeesByTeam(
            @Parameter(description = "ID của Team (Tổ)") @PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeesByTeam(teamId)));
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('staff_organization.view')")
    @Operation(summary = "Lấy danh sách Tài khoản (Users)", description = "Trả về danh sách tất cả tài khoản người dùng trên hệ thống (dạng Dashboard)")
    public ResponseEntity<ApiResponse<List<UserDashboard>>> getUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUserDashboard()));
    }

    @GetMapping("/employees")
    @PreAuthorize("hasAuthority('staff_organization.view')")
    @Operation(summary = "Lấy danh sách Nhân viên (Employees)", description = "Trả về danh sách tất cả nhân viên")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployees() {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getAllEmployees()));
    }

    @GetMapping("/employees/no-account")
    @PreAuthorize("hasAuthority('staff_organization.view')")
    @Operation(summary = "Lấy danh sách Nhân viên chưa có tài khoản", description = "Dùng để hiển thị dropdown list khi tạo mới User")
    public ResponseEntity<ApiResponse<List<EmployeeNoAccountDTO>>> getEmployeesWithoutAccount() {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeesWithoutAccount()));
    }

    // ====================== CREATE ======================

    @PostMapping("/sections")
    @PreAuthorize("hasAuthority('staff_organization.create')")
    @Operation(summary = "Tạo mới Phòng/Ban (Section)")
    public ResponseEntity<ApiResponse<SectionResponse>> createSection(@RequestBody SectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sectionService.createSection(request)));
    }

    @PostMapping("/groups")
    @PreAuthorize("hasAuthority('staff_organization.create')")
    @Operation(summary = "Tạo mới Nhóm (Group)")
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(@RequestBody GroupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(groupService.createGroup(request)));
    }

    @PostMapping("/teams")
    @PreAuthorize("hasAuthority('staff_organization.create')")
    @Operation(summary = "Tạo mới Tổ (Team)")
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(@RequestBody TeamRequest request) {
        return ResponseEntity.ok(ApiResponse.success(teamService.createTeam(request)));
    }

    @PostMapping("/employees")
    @PreAuthorize("hasAuthority('staff_organization.create')")
    @Operation(summary = "Tạo mới Nhân viên (Employee)", description = "Bắt buộc điền đúng các trường validate")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.createEmployee(request)));
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('staff_organization.create')")
    @Operation(summary = "Tạo mới Tài khoản (User)", description = "Sẽ tự động gửi email chứa password ngẫu nhiên về email của nhân viên")
    public ResponseEntity<ApiResponse<UserDashboard>> createUser(@RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.createUser(request)));
    }

    // ====================== UPDATE ======================

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('staff_organization.edit')")
    @Operation(summary = "Cập nhật Tài khoản", description = "Cho phép cập nhật Email, quyền (Role) và trạng thái hoạt động (isActive)")
    public ResponseEntity<ApiResponse<UserDashboard>> updateUser(
            @Parameter(description = "ID của User") @PathVariable Long id,
            @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.updateUser(id, request)));
    }

    @PutMapping("/sections/{id}")
    @PreAuthorize("hasAuthority('staff_organization.edit')")
    @Operation(summary = "Cập nhật Phòng/Ban (Section)")
    public ResponseEntity<ApiResponse<SectionResponse>> updateSection(
            @Parameter(description = "ID của Section") @PathVariable Long id,
            @RequestBody SectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sectionService.updateSection(id, request)));
    }

    @PutMapping("/groups/{id}")
    @PreAuthorize("hasAuthority('staff_organization.edit')")
    @Operation(summary = "Cập nhật Nhóm (Group)")
    public ResponseEntity<ApiResponse<GroupResponse>> updateGroup(
            @Parameter(description = "ID của Group") @PathVariable Long id,
            @RequestBody GroupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(groupService.updateGroup(id, request)));
    }

    @PutMapping("/teams/{id}")
    @PreAuthorize("hasAuthority('staff_organization.edit')")
    @Operation(summary = "Cập nhật Tổ (Team)")
    public ResponseEntity<ApiResponse<TeamResponse>> updateTeam(
            @Parameter(description = "ID của Team") @PathVariable Long id,
            @RequestBody TeamRequest request) {
        return ResponseEntity.ok(ApiResponse.success(teamService.updateTeam(id, request)));
    }

    @PutMapping("/employees/{id}")
    @PreAuthorize("hasAuthority('staff_organization.edit')")
    @Operation(summary = "Cập nhật Nhân viên (Employee)")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @Parameter(description = "ID của Employee") @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.updateEmployee(id, request)));
    }

    // ====================== DELETE ======================

    @DeleteMapping("/sections/{id}")
    @PreAuthorize("hasAuthority('staff_organization.delete')")
    @Operation(summary = "Xóa Phòng/Ban (Section)")
    public ResponseEntity<Void> deleteSection(
            @Parameter(description = "ID của Section") @PathVariable Long id) {
        sectionService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/groups/{id}")
    @PreAuthorize("hasAuthority('staff_organization.delete')")
    @Operation(summary = "Xóa Nhóm (Group)")
    public ResponseEntity<Void> deleteGroup(
            @Parameter(description = "ID của Group") @PathVariable Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/teams/{id}")
    @PreAuthorize("hasAuthority('staff_organization.delete')")
    @Operation(summary = "Xóa Tổ (Team)")
    public ResponseEntity<Void> deleteTeam(
            @Parameter(description = "ID của Team") @PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/employees/{id}")
    @PreAuthorize("hasAuthority('staff_organization.delete')")
    @Operation(summary = "Xóa Nhân viên (Employee)", description = "Xóa mềm (Soft Delete) nhân viên và tự động khóa tài khoản User liên kết")
    public ResponseEntity<ApiResponse<String>> deleteEmployee(
            @Parameter(description = "ID của Employee") @PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa nhân viên thành công"));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('staff_organization.delete')")
    @Operation(summary = "Xóa Tài khoản (User)")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID của User") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}