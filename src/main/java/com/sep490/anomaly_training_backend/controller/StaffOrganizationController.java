package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.EmployeeRequest;
import com.sep490.anomaly_training_backend.dto.request.GroupRequest;
import com.sep490.anomaly_training_backend.dto.request.SectionRequest;
import com.sep490.anomaly_training_backend.dto.request.TeamRequest;
import com.sep490.anomaly_training_backend.dto.request.UserCreateRequest;
import com.sep490.anomaly_training_backend.dto.request.UserUpdateRequest;
import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.EmployeeTrainingHistoryResponse;
import com.sep490.anomaly_training_backend.dto.response.EmployeeNoAccountDTO;
import com.sep490.anomaly_training_backend.dto.response.EmployeeResponse;
import com.sep490.anomaly_training_backend.dto.response.GroupResponse;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.dto.response.SectionResponse;
import com.sep490.anomaly_training_backend.dto.response.TeamResponse;
import com.sep490.anomaly_training_backend.dto.response.UserDashboard;
import com.sep490.anomaly_training_backend.dto.response.UserResponse;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.EmployeeService;
import com.sep490.anomaly_training_backend.service.TrainingResultService;
import com.sep490.anomaly_training_backend.service.GroupService;
import com.sep490.anomaly_training_backend.service.SectionService;
import com.sep490.anomaly_training_backend.service.TeamService;
import com.sep490.anomaly_training_backend.service.account.UserService;
import com.sep490.anomaly_training_backend.service.impl.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff-organization")
@RequiredArgsConstructor
@Tag(name = "Staff Organization API", description = "Manage staff organization structure (Departments, Groups, Teams, Employees, Users)")
public class StaffOrganizationController {

    private final SectionService sectionService;
    private final GroupService groupService;
    private final TeamService teamService;
    private final EmployeeService employeeService;
    private final UserService userService;
    private final AuthService authService;
    private final TrainingResultService trainingResultService;

    // ====================== VIEW ======================

    @GetMapping("/sections")
    @PreAuthorize("hasAuthority('staff_organization.view')")
    @Operation(summary = "Get list of Sections/Departments", description = "Returns a list of all departments in the system")
    public ResponseEntity<ApiResponse<List<SectionResponse>>> getSections() {
        return ResponseEntity.ok(ApiResponse.success(sectionService.getAllSections()));
    }

    @GetMapping("/group/{sectionId}")
    @PreAuthorize("hasAuthority('staff_organization.view')")
    @Operation(summary = "Get list of Groups by Section ID", description = "Pass the Section ID to get a list of groups belonging to that section")
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getGroupsBySection(
            @Parameter(description = "ID của Section (Phòng/Ban)") @PathVariable Long sectionId) {
        return ResponseEntity.ok(ApiResponse.success(groupService.getGroupsBySection(sectionId)));
    }

    @GetMapping("/team/{groupId}")
    @PreAuthorize("hasAuthority('staff_organization.view')")
    @Operation(summary = "Get list of Teams by Group ID", description = "Pass the Group ID to get a list of teams belonging to that group")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getTeamsByGroup(
            @Parameter(description = "ID của Group (Nhóm)") @PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getTeamsByGroup(groupId)));
    }

    @GetMapping("/member")
    @PreAuthorize("hasAuthority('team.manage')")
    @Operation(summary = "Get list of Employees by Team ID", description = "Pass the Team ID to get a list of employees belonging to that team")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployeesUnderManagement(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeesUnderManagement(currentUser)));
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('staff_organization.view')")
    @Operation(summary = "Get list of Accounts (Users)", description = "Returns a list of all user accounts in the system (Dashboard format)")
    public ResponseEntity<ApiResponse<List<UserDashboard>>> getUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUserDashboard()));
    }

    @GetMapping("/employees")
    @PreAuthorize("hasAuthority('staff_organization.view')")
    @Operation(summary = "Get list of Employees", description = "Returns a list of all employees")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployees() {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getAllEmployees()));
    }

    @GetMapping("/employees/no-account")
    @PreAuthorize("hasAuthority('staff_organization.view')")
    @Operation(summary = "Get list of Employees without accounts", description = "Used to display dropdown list when creating a new User")
    public ResponseEntity<ApiResponse<List<EmployeeNoAccountDTO>>> getEmployeesWithoutAccount() {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeesWithoutAccount()));
    }

    @GetMapping("/product-line/team-lead/{productLineId}")
    @PreAuthorize("hasAuthority('staff_organization.view')")
    @Operation(summary = "Get list of Teams on product line", description = "Used to display dropdown list when creating a new User")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getTeamLeadInProductLine(@PathVariable Long productLineId) {
        List<UserResponse> result = userService.getTeamLeadInProductLine(productLineId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/process/{processId}/employeeSkills")
    @PreAuthorize("hasAuthority('staff_organization.view')")
    @Operation(summary = "Get list of Employees with skills in process", description = "Used to get list employee skills")
    public ResponseEntity<ApiResponse<ProcessResponse>> getEmployeeSkillsByProcess(
            @PathVariable Long processId
    ) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeesByProcess(processId)));
    }

    @GetMapping("/employee/{employeeId}/training-history")
    @PreAuthorize("hasAuthority('staff_organization.view')")
    @Operation(summary = "Get list of history training", description = "Used to get list employee training history")
    public ResponseEntity<ApiResponse<EmployeeTrainingHistoryResponse>> getEmployeeTrainingHistory(
            @PathVariable Long employeeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(trainingResultService.getEmployeeTrainingHistory(employeeId)));
    }

    // ====================== CREATE ======================

    @PostMapping("/sections")
    @PreAuthorize("hasAuthority('staff_organization.manage')")
    @Operation(summary = "Create new Section/Department")
    public ResponseEntity<ApiResponse<SectionResponse>> createSection(@RequestBody SectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sectionService.createSection(request)));
    }

    @PostMapping("/groups")
    @PreAuthorize("hasAuthority('staff_organization.manage')")
    @Operation(summary = "Create new Group")
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(@RequestBody GroupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(groupService.createGroup(request)));
    }

    @PostMapping("/teams")
    @PreAuthorize("hasAuthority('staff_organization.manage')")
    @Operation(summary = "Create new Team")
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(@RequestBody TeamRequest request) {
        return ResponseEntity.ok(ApiResponse.success(teamService.createTeam(request)));
    }

    @PostMapping("/employees")
    @PreAuthorize("hasAuthority('staff_organization.manage')")
    @Operation(summary = "Create new Employee", description = "Must fill in all required fields correctly")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.createEmployee(request)));
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('staff_organization.manage')")
    @Operation(summary = "Create new Account (User)", description = "Will automatically send an email with a random password to the employee's email address")
    public ResponseEntity<ApiResponse<UserDashboard>> createUser(@RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.createUser(request)));
    }

    // ====================== UPDATE ======================

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('staff_organization.manage')")
    @Operation(summary = "Update Account", description = "Allows updating Email, permissions (Roles) and active status (isActive)")
    public ResponseEntity<ApiResponse<UserDashboard>> updateUser(
            @Parameter(description = "ID của User") @PathVariable Long id,
            @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.updateUser(id, request)));
    }

    @PutMapping("/sections/{id}")
    @PreAuthorize("hasAuthority('staff_organization.manage')")
    @Operation(summary = "Update Section/Department")
    public ResponseEntity<ApiResponse<SectionResponse>> updateSection(
            @Parameter(description = "ID của Section") @PathVariable Long id,
            @RequestBody SectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sectionService.updateSection(id, request)));
    }

    @PutMapping("/groups/{id}")
    @PreAuthorize("hasAuthority('staff_organization.manage')")
    @Operation(summary = "Update Group")
    public ResponseEntity<ApiResponse<GroupResponse>> updateGroup(
            @Parameter(description = "ID của Group") @PathVariable Long id,
            @RequestBody GroupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(groupService.updateGroup(id, request)));
    }

    @PutMapping("/teams/{id}")
    @PreAuthorize("hasAuthority('staff_organization.manage')")
    @Operation(summary = "Update Team")
    public ResponseEntity<ApiResponse<TeamResponse>> updateTeam(
            @Parameter(description = "ID của Team") @PathVariable Long id,
            @RequestBody TeamRequest request) {
        return ResponseEntity.ok(ApiResponse.success(teamService.updateTeam(id, request)));
    }

    @PutMapping("/employees/{id}")
    @PreAuthorize("hasAuthority('staff_organization.manage')")
    @Operation(summary = "Update Employee")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @Parameter(description = "ID của Employee") @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.updateEmployee(id, request)));
    }

    // ====================== DELETE ======================

    @DeleteMapping("/sections/{id}")
    @PreAuthorize("hasAuthority('staff_organization.manage')")
    @Operation(summary = "Delete Section/Department")
    public ResponseEntity<Void> deleteSection(
            @Parameter(description = "ID của Section") @PathVariable Long id) {
        sectionService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/groups/{id}")
    @PreAuthorize("hasAuthority('staff_organization.manage')")
    @Operation(summary = "delete(Group)")
    public ResponseEntity<Void> deleteGroup(
            @Parameter(description = "ID của Group") @PathVariable Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/remove-group-from-section/{groupId}")
    @PreAuthorize("hasAuthority('staff_organization.manage')")
    @Operation(summary = "delete(Group)")
    public ResponseEntity<Void> removeGroupFromSection(
            @Parameter(description = "ID của Group") @PathVariable Long groupId) {
        groupService.removeGroupFromSection(groupId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/teams/{id}")
    @PreAuthorize("hasAuthority('staff_organization.manage')")
    @Operation(summary = "delete (Team)")
    public ResponseEntity<Void> deleteTeam(
            @Parameter(description = "ID của Team") @PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/remove-team-from-group/{teamId}")
    @PreAuthorize("hasAuthority('staff_organization.manage')")
    @Operation(summary = "delete (Team)")
    public ResponseEntity<Void> removeTeamFromGroup(
            @Parameter(description = "ID của Team") @PathVariable Long teamId) {
        teamService.deleteTeamFromGroup(teamId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/employees/{id}")
    @PreAuthorize("hasAuthority('staff_organization.manage')")
    @Operation(summary = "Delete (Employee)", description = "Xóa mềm (Soft Delete) nhân viên và tự động khóa tài khoản User liên kết")
    public ResponseEntity<ApiResponse<String>> deleteEmployee(
            @Parameter(description = "ID của Employee") @PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa nhân viên thành công"));
    }

    @PutMapping("/teams/{id}/remove-employees")
    @PreAuthorize("hasAuthority('staff_organization.manage')")
    @Operation(summary = "Remove Employees from Team", description = "Used to remove employees from team")
    public ResponseEntity<ApiResponse<Void>> removeEmployeesFromTeam(
            @Parameter(description = "ID của Team") @PathVariable Long id,
            @RequestBody List<Long> employeeIds
    ) {
        employeeService.removeEmployeesFromTeam(id, employeeIds);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PutMapping("/teams/{id}/add-employees")
    @PreAuthorize("hasAuthority('staff_organization.manage')")
    @Operation(summary = "Add Employees to Team", description = "Add existing employees to a team")
    public ResponseEntity<ApiResponse<Void>> addEmployeesToTeam(
            @Parameter(description = "ID của Team") @PathVariable Long id,
            @RequestBody List<Long> employeeIds
    ) {
        employeeService.addEmployeesToTeam(id, employeeIds);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('staff_organization.manage')")
    @Operation(summary = "Delete (User)")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID của User") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}