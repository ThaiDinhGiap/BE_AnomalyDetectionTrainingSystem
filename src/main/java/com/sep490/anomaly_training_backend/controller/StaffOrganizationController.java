package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.EmployeeRequest;
import com.sep490.anomaly_training_backend.dto.request.GroupRequest;
import com.sep490.anomaly_training_backend.dto.request.SectionRequest;
import com.sep490.anomaly_training_backend.dto.request.TeamRequest;
import com.sep490.anomaly_training_backend.dto.response.*;
import com.sep490.anomaly_training_backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff-organization")
@RequiredArgsConstructor
public class StaffOrganizationController {
    private final SectionService sectionService;
    private final GroupService groupService;
    private final TeamService teamService;
    private final EmployeeService employeeService;
    private final UserService userService;

    // ====================== VIEW ======================
    @GetMapping("/sections")
    @PreAuthorize("hasAuthority('staff-organization.view')")
    public ResponseEntity<ApiResponse<List<SectionResponse>>> getSections() {
        return ResponseEntity.ok(ApiResponse.success(sectionService.getAllSections()));
    }

    @GetMapping("/group/{sectionId}")
    @PreAuthorize("hasAuthority('staff-organization.view')")
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getGroupsBySection(@PathVariable Long sectionId) {
        return ResponseEntity.ok(ApiResponse.success(groupService.getGroupsBySection(sectionId)));
    }
    @GetMapping("/team/{groupId}")
    @PreAuthorize("hasAuthority('staff-organization.view')")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getTeamsByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getTeamsByGroup(groupId)));
    }
    @GetMapping("/member/{teamId}")
    @PreAuthorize("hasAuthority('staff-organization.view')")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployeesByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeesByTeam(teamId)));
    }
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('staff-organization.view')") //API dành cho danh sách tài khoản
    public ResponseEntity<ApiResponse<List<UserDashboard>>> getUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUserDashboard()));
    }

    @GetMapping("/employees")
    @PreAuthorize("hasAuthority('staff-organization.view')") //API dành cho Danh sách công nhân
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployees() {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getAllEmployees()));
    }
    // ====================== CREATE ======================
    @PostMapping("/sections")
    @PreAuthorize("hasAuthority('staff-organization.create')")
    public ResponseEntity<ApiResponse<SectionResponse>> createSection(@RequestBody SectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sectionService.createSection(request)));
    }

    @PostMapping("/groups")
    @PreAuthorize("hasAuthority('staff-organization.create')")
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(@RequestBody GroupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(groupService.createGroup(request)));
    }
    @PostMapping("/teams")
    @PreAuthorize("hasAuthority('staff-organization.create')")
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(@RequestBody TeamRequest request) {
        return ResponseEntity.ok(ApiResponse.success(teamService.createTeam(request)));
    }
    @PostMapping("/employees")
    @PreAuthorize("hasAuthority('staff-organization.create')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(@RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.createEmployee(request)));
    }
    // ====================== UPDATE ======================
    @PutMapping("/sections/{id}")
    @PreAuthorize("hasAuthority('staff-organization.edit')")
    public ResponseEntity<ApiResponse<SectionResponse>> updateSection(
            @PathVariable Long id,
            @RequestBody SectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sectionService.updateSection(id, request)));
    }

    @PutMapping("/groups/{id}")
    @PreAuthorize("hasAuthority('admin.edit')")
    public ResponseEntity<ApiResponse<GroupResponse>> updateGroup(
            @PathVariable Long id,
            @RequestBody GroupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(groupService.updateGroup(id, request)));
    }

    @PutMapping("/teams/{id}")
    @PreAuthorize("hasAuthority('admin.edit')")
    public ResponseEntity<ApiResponse<TeamResponse>> updateTeam(
            @PathVariable Long id,
            @RequestBody TeamRequest request) {
        return ResponseEntity.ok(ApiResponse.success(teamService.updateTeam(id, request)));
    }

    @PutMapping("/employees/{id}")
    @PreAuthorize("hasAuthority('admin.edit')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long id,
            @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.updateEmployee(id, request)));
    }
    // ====================== DELETE ======================

    @DeleteMapping("/sections/{id}")
    @PreAuthorize("hasAuthority('staff-organization.delete')")
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
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('admin.delete')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
