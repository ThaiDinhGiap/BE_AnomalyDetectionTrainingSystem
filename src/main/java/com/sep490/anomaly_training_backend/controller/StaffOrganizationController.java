package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.EmployeeRequest;
import com.sep490.anomaly_training_backend.dto.request.GroupRequest;
import com.sep490.anomaly_training_backend.dto.request.SectionRequest;
import com.sep490.anomaly_training_backend.dto.request.TeamRequest;
import com.sep490.anomaly_training_backend.dto.request.UserCreateRequest;
import com.sep490.anomaly_training_backend.dto.request.UserUpdateRequest;
import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.EmployeeNoAccountDTO;
import com.sep490.anomaly_training_backend.dto.response.EmployeeResponse;
import com.sep490.anomaly_training_backend.dto.response.EmployeeSkillResponse;
import com.sep490.anomaly_training_backend.dto.response.EmployeeTrainingHistoryResponse;
import com.sep490.anomaly_training_backend.dto.response.GroupResponse;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.dto.response.SectionResponse;
import com.sep490.anomaly_training_backend.dto.response.TeamResponse;
import com.sep490.anomaly_training_backend.dto.response.UserDashboard;
import com.sep490.anomaly_training_backend.dto.response.UserResponse;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.EmployeeService;
import com.sep490.anomaly_training_backend.service.EmployeeSkillService;
import com.sep490.anomaly_training_backend.service.GroupService;
import com.sep490.anomaly_training_backend.service.SectionService;
import com.sep490.anomaly_training_backend.service.TeamService;
import com.sep490.anomaly_training_backend.service.TrainingResultService;
import com.sep490.anomaly_training_backend.service.account.UserService;
import com.sep490.anomaly_training_backend.service.impl.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.io.IOException;
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
    private final EmployeeSkillService employeeSkillService;
    private final TrainingResultService trainingResultService;

    // For Admin
    @GetMapping("/sections")
    @PreAuthorize("hasAuthority('staff_structure.view')")
    @Operation(summary = "Get list of Sections/Departments", description = "Returns a list of all departments in the system")
    public ResponseEntity<ApiResponse<List<SectionResponse>>> getSections() {
        return ResponseEntity.ok(ApiResponse.success(sectionService.getAllSections()));
    }

    @GetMapping("/employees")
    @PreAuthorize("hasAuthority('employee.view_all')")
    @Operation(summary = "Get list of Employees", description = "Returns a list of all employees")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployees() {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getAllEmployees()));
    }

    @PostMapping("/sections")
    @PreAuthorize("hasAuthority('staff_structure.manage')")
    @Operation(summary = "Create new Section/Department")
    public ResponseEntity<ApiResponse<SectionResponse>> createSection(@RequestBody SectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sectionService.createSection(request)));
    }

    @PutMapping("/sections/{id}")
    @PreAuthorize("hasAuthority('staff_structure.manage')")
    @Operation(summary = "Update Section/Department")
    public ResponseEntity<ApiResponse<SectionResponse>> updateSection(
            @Parameter(description = "ID của Section") @PathVariable Long id,
            @RequestBody SectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sectionService.updateSection(id, request)));
    }

    @PostMapping("/groups")
    @PreAuthorize("hasAuthority('staff_structure.manage')")
    @Operation(summary = "Create new Group")
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(@RequestBody GroupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(groupService.createGroup(request)));
    }

    @PutMapping("/groups/{id}")
    @PreAuthorize("hasAuthority('staff_structure.manage')")
    @Operation(summary = "Update Group")
    public ResponseEntity<ApiResponse<GroupResponse>> updateGroup(
            @Parameter(description = "ID của Group") @PathVariable Long id,
            @RequestBody GroupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(groupService.updateGroup(id, request)));
    }

    @PostMapping("/teams")
    @PreAuthorize("hasAuthority('staff_structure.manage')")
    @Operation(summary = "Create new Team")
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(@RequestBody TeamRequest request) {
        return ResponseEntity.ok(ApiResponse.success(teamService.createTeam(request)));
    }

    @PutMapping("/teams/{id}")
    @PreAuthorize("hasAuthority('staff_structure.manage')")
    @Operation(summary = "Update Team")
    public ResponseEntity<ApiResponse<TeamResponse>> updateTeam(
            @Parameter(description = "ID của Team") @PathVariable Long id,
            @RequestBody TeamRequest request) {
        return ResponseEntity.ok(ApiResponse.success(teamService.updateTeam(id, request)));
    }

    @DeleteMapping("/sections/{id}")
    @PreAuthorize("hasAuthority('staff_structure.manage')")
    @Operation(summary = "Delete Section/Department")
    public ResponseEntity<Void> deleteSection(
            @Parameter(description = "ID của Section") @PathVariable Long id) {
        sectionService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/groups/{id}")
    @PreAuthorize("hasAuthority('staff_structure.manage')")
    @Operation(summary = "delete(Group)")
    public ResponseEntity<Void> deleteGroup(
            @Parameter(description = "ID của Group") @PathVariable Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/groups/{groupId}/section")
    @PreAuthorize("hasAuthority('staff_structure.manage')")
    @Operation(summary = "delete(Group)")
    public ResponseEntity<Void> removeGroupFromSection(
            @Parameter(description = "ID của Group") @PathVariable Long groupId) {
        groupService.removeGroupFromSection(groupId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/teams/{id}")
    @PreAuthorize("hasAuthority('staff_structure.manage')")
    @Operation(summary = "delete (Team)")
    public ResponseEntity<Void> deleteTeam(
            @Parameter(description = "ID của Team") @PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/teams/{teamId}/group")
    @PreAuthorize("hasAuthority('staff_structure.manage')")
    @Operation(summary = "delete (Team)")
    public ResponseEntity<Void> removeTeamFromGroup(
            @Parameter(description = "ID của Team") @PathVariable Long teamId) {
        teamService.deleteTeamFromGroup(teamId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/employees/{id}")
    @PreAuthorize("hasAuthority('employee.manage')")
    @Operation(summary = "Delete (Employee)", description = "Xóa mềm (Soft Delete) nhân viên và tự động khóa tài khoản User liên kết")
    public ResponseEntity<ApiResponse<String>> deleteEmployee(
            @Parameter(description = "ID của Employee") @PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa nhân viên thành công"));
    }

    @DeleteMapping("/teams/{id}/employees")
    @PreAuthorize("hasAuthority('employee.manage')")
    @Operation(summary = "Remove Employees from Team", description = "Used to remove employees from team")
    public ResponseEntity<ApiResponse<Void>> removeEmployeesFromTeam(
            @Parameter(description = "ID của Team") @PathVariable Long id,
            @RequestBody List<Long> employeeIds
    ) {
        employeeService.removeEmployeesFromTeam(id, employeeIds);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/employees")
    @PreAuthorize("hasAuthority('employee.manage')")
    @Operation(summary = "Create new Employee", description = "Must fill in all required fields correctly")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.createEmployee(request)));
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('user.manage')")
    @Operation(summary = "Create new Account (User)", description = "Will automatically send an email with a random password to the employee's email address")
    public ResponseEntity<ApiResponse<UserDashboard>> createUser(@RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.createUser(request)));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('user.manage')")
    @Operation(summary = "Update Account", description = "Allows updating Email, permissions (Roles) and active status (isActive)")
    public ResponseEntity<ApiResponse<UserDashboard>> updateUser(
            @Parameter(description = "ID của User") @PathVariable Long id,
            @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.updateUser(id, request)));
    }

    @PutMapping("/users/{id}/reset-password")
    @PreAuthorize("hasAuthority('user.manage')")
    @Operation(summary = "Reset Account Password", description = "Reset user password to an auto-generated one and force password change on next login")
    public ResponseEntity<ApiResponse<String>> resetUserPassword(
            @Parameter(description = "ID của User") @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Success", authService.resetUserPassword(id)));
    }

    @PutMapping("/employees/{id}")
    @PreAuthorize("hasAuthority('employee.manage')")
    @Operation(summary = "Update Employee")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @Parameter(description = "ID của Employee") @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.updateEmployee(id, request)));
    }

    @PostMapping("/teams/{id}/employees")
    @PreAuthorize("hasAuthority('employee.manage')")
    @Operation(summary = "Add Employees to Team", description = "Add existing employees to a team")
    public ResponseEntity<ApiResponse<Void>> addEmployeesToTeam(
            @Parameter(description = "ID của Team") @PathVariable Long id,
            @RequestBody List<Long> employeeIds
    ) {
        employeeService.addEmployeesToTeam(id, employeeIds);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('user.manage')")
    @Operation(summary = "Delete (User)")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID của User") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('user.view')")
    @Operation(summary = "Get list of Accounts (Users)", description = "Returns a list of all user accounts in the system (Dashboard format)")
    public ResponseEntity<ApiResponse<List<UserDashboard>>> getUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUserDashboard()));
    }

    @GetMapping("/employees/no-account")
    @PreAuthorize("hasAuthority('user.view')")
    @Operation(summary = "Get list of Employees without accounts", description = "Used to display dropdown list when creating a new User")
    public ResponseEntity<ApiResponse<List<EmployeeNoAccountDTO>>> getEmployeesWithoutAccount() {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeesWithoutAccount()));
    }


    @GetMapping("/product-lines/{productLineId}/team-leads")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get list of Teams on product line", description = "Used to display dropdown list when creating a new User")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getTeamLeadInProductLine(@PathVariable Long productLineId) {
        List<UserResponse> result = userService.getTeamLeadInProductLine(productLineId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // For TL/SV/MG
    @GetMapping("/members")
    @PreAuthorize("hasAuthority('employee.view')")
    @Operation(summary = "Get list of Employees by Team ID", description = "Pass the Team ID to get a list of employees belonging to that team")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployeesUnderManagement(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeesUnderManagement(currentUser)));
    }

    @GetMapping("/processes/{processId}/employee-skills")
    @PreAuthorize("hasAuthority('employee_skill.view')")
    @Operation(summary = "Get list of Employees with skills in process", description = "Used to get list employee skills")
    public ResponseEntity<ApiResponse<ProcessResponse>> getEmployeeSkillsByProcess(
            @PathVariable Long processId
    ) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeesByProcess(processId)));
    }

    @GetMapping("/employees/{employeeId}/training-history")
    @PreAuthorize("hasAuthority('employee.view')")
    @Operation(summary = "Get list of history training", description = "Used to get list employee training history")
    public ResponseEntity<ApiResponse<EmployeeTrainingHistoryResponse>> getEmployeeTrainingHistory(
            @PathVariable Long employeeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(trainingResultService.getEmployeeTrainingHistory(employeeId)));
    }

    @GetMapping("/employees/{employeeId}/skills")
    @PreAuthorize("hasAuthority('employee_skill.view')")
    @Operation(summary = "Get list of history training", description = "Used to get list employee skills")
    public ResponseEntity<ApiResponse<List<EmployeeSkillResponse>>> getEmployeesSkillByEmployee(
            @PathVariable Long employeeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(employeeSkillService.getEmployeeSkillsByEmployeeId(employeeId)));
    }

    @Operation(summary = "Download employee skill template")
    @GetMapping("/employee-skill/download-template")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadEmployeeSkillTemplate() throws IOException {
        ClassPathResource file = new ClassPathResource("templates/excel/Employee_skill_guideline.xlsx");

        if (!file.exists()) {
            throw new FileNotFoundException("Không tìm thấy file template Excel");
        }
        Resource resource = new InputStreamResource(file.getInputStream());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Employee_skill_guideline.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .contentLength(file.contentLength())
                .body(resource);
    }
}