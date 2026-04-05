package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.EmployeeImportDto;
import com.sep490.anomaly_training_backend.dto.request.EmployeeRequest;
import com.sep490.anomaly_training_backend.dto.response.EmployeeNoAccountDTO;
import com.sep490.anomaly_training_backend.dto.response.EmployeeResponse;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.enums.EmployeeStatus;
import com.sep490.anomaly_training_backend.enums.ImportStatus;
import com.sep490.anomaly_training_backend.enums.ImportType;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.EmployeeMapper;
import com.sep490.anomaly_training_backend.mapper.EmployeeSkillMapper;
import com.sep490.anomaly_training_backend.mapper.ProcessMapper;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.EmployeeService;
import com.sep490.anomaly_training_backend.service.ImportHistoryService;
import com.sep490.anomaly_training_backend.util.SecurityUtils;
import com.sep490.anomaly_training_backend.util.helper.EmployeeImportHelper;
import com.sep490.anomaly_training_backend.util.validator.EmployeeImportValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static com.sep490.anomaly_training_backend.util.SecurityUtils.hasPermission;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final TeamRepository teamRepository;
    private final ProcessRepository processRepository;
    private final TrainingResultDetailRepository trainingResultDetailRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final EmployeeSkillMapper employeeSkillMapper;
    private final ProcessMapper processMapper;

    private final EmployeeImportHelper importHelper;
    private final EmployeeImportValidator importValidator;
    private final ImportHistoryService importHistoryService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        String empCode = request.getEmployeeCode() != null ? request.getEmployeeCode().trim() : null;

        if (empCode != null && employeeRepository.existsByEmployeeCode(empCode)) {
            throw new AppException(ErrorCode.EMPLOYEE_CODE_ALREADY_LINKED);
        }

        if (request.getTeamIds() != null) {
            boolean isTeamExist = teamRepository.existsById(request.getTeamIds().get(0));
            if (!isTeamExist) {
                throw new AppException(ErrorCode.TEAM_NOT_FOUND);
            }
        }

        Employee employee = employeeMapper.toEntity(request);
        employee.setEmployeeCode(empCode);

        if (employee.getStatus() == null) {
            employee.setStatus(EmployeeStatus.ACTIVE);
        }

        return employeeMapper.toDTO(employeeRepository.save(employee));
    }

    // ================= Import Employee =================

    @Override
    @Transactional
    public List<EmployeeResponse> importEmployee(User user, MultipartFile employeeFile) {
        List<ImportErrorItem> errors = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(employeeFile.getInputStream())) {
            validateImportFile(employeeFile);
            Sheet sheet = getFirstSheet(workbook);

            // Step 1: Parse rows
            List<EmployeeImportDto> parsedRows = importHelper.parseExcelRows(sheet, errors);

            if (!errors.isEmpty()) {
                saveImportFailHistory(user, employeeFile, errors);
                throw new AppException(ErrorCode.IMPORT_PARSE_ERROR);
            }

            // Step 2: Validate file data (NO DB check)
            importValidator.validateFileData(parsedRows, errors);

            if (!errors.isEmpty()) {
                saveImportFailHistory(user, employeeFile, errors);
                throw new AppException(ErrorCode.IMPORT_VALIDATION_ERROR);
            }

            // Step 3: Process all rows
            List<EmployeeResponse> responses = processAllRows(parsedRows);

            // Step 4: Save success history
            saveImportPassHistory(user, employeeFile);

            return responses;

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Import employee failed", e);
            if (errors.isEmpty()) {
                errors.add(buildSystemError("System error: " + e.getMessage()));
                saveImportFailHistory(user, employeeFile, errors);
            }
            throw new AppException(ErrorCode.CANNOT_READ_EXCEL_FILE);
        }
    }

    /**
     * Process all rows with proper error handling
     * - Finds existing or creates new Employee
     * - Creates User with Role if role is specified
     */
    private List<EmployeeResponse> processAllRows(List<EmployeeImportDto> parsedRows) {
        List<EmployeeResponse> responses = new ArrayList<>();

        for (EmployeeImportDto dto : parsedRows) {
            // Step 1: Find or create Employee by employeeCode
            Employee employee = findOrCreateEmployee(dto);
            // Step 2: Update Employee fields
            updateEmployeeFields(employee, dto);
            employee = employeeRepository.save(employee);
            // Step 3: Handle User creation (only if role is not blank)
            if (dto.getRole() != null && !dto.getRole().trim().isEmpty()) {
                createOrUpdateUserWithRole(dto);
            }
            // Step 4: Build response
            responses.add(enrichEmployeeData(employee));
        }
        return responses;
    }

    /**
     * Find existing Employee or create new one
     * - Lookup by employeeCode
     * - If found: return existing (will be updated)
     * - If not found: create new
     */
    private Employee findOrCreateEmployee(EmployeeImportDto dto) {
        return employeeRepository.findByEmployeeCode(dto.getEmployeeCode())
                .orElseGet(Employee::new);
    }

    /**
     * Update all Employee fields from DTO
     */
    private void updateEmployeeFields(Employee employee, EmployeeImportDto dto) {
        employee.setEmployeeCode(dto.getEmployeeCode());
        employee.setFullName(dto.getFullName());
    }

    /**
     * Create or update User with Role (only called when role is not blank)
     */
    private void createOrUpdateUserWithRole(EmployeeImportDto dto) {
        String username = "VN" + dto.getEmployeeCode();
        String rawPassword = "ADTMS@" + dto.getEmployeeCode();
        String roleCode = EmployeeImportValidator.mapRoleDisplayToCode(dto.getRole());

        Role role = roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        // Find or create User
        User newUser = userRepository.findByEmployeeCodeAndDeleteFlagFalse(dto.getEmployeeCode())
                .orElseGet(() -> User.builder()
                        .username(username)
                        .passwordHash(passwordEncoder.encode(rawPassword))
                        .employeeCode(dto.getEmployeeCode())
                        .fullName(dto.getFullName())
                        .email(dto.getEmail())
                        .build());

        // Update fields (in case user already exists)
        newUser.setFullName(dto.getFullName());
        newUser.setEmail(dto.getEmail());

        // Set role
        newUser.getRoles().clear();
        newUser.getRoles().add(role);

        userRepository.save(newUser);
    }

    /**
     * Validate import file
     */
    private void validateImportFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_IS_EMPTY);
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null
                || (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls"))) {
            throw new AppException(ErrorCode.INVALID_FILE_FORMAT);
        }
    }

    /**
     * Get first sheet from workbook
     */
    private Sheet getFirstSheet(Workbook workbook) {
        if (workbook.getNumberOfSheets() == 0) {
            throw new AppException(ErrorCode.EXCEL_SHEET_NOT_FOUND);
        }

        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            throw new AppException(ErrorCode.EXCEL_SHEET_NOT_FOUND);
        }

        return sheet;
    }

    /**
     * Save import fail history
     */
    private void saveImportFailHistory(User user, MultipartFile file, List<ImportErrorItem> errors) {
        try {
            importHistoryService.saveHistory(
                    user,
                    file.getOriginalFilename(),
                    ImportType.EMPLOYEE_IMPORT,
                    ImportStatus.FAIL,
                    errors);
        } catch (Exception e) {
            log.error("Error saving import fail history: {}", e.getMessage());
        }
    }

    /**
     * Save import pass history
     */
    private void saveImportPassHistory(User user, MultipartFile file) {
        try {
            importHistoryService.saveHistory(
                    user,
                    file.getOriginalFilename(),
                    ImportType.EMPLOYEE_IMPORT,
                    ImportStatus.PASS,
                    List.of());
        } catch (Exception e) {
            log.error("Error saving import pass history: {}", e.getMessage());
        }
    }

    /**
     * Build system error item
     */
    private ImportErrorItem buildSystemError(String message) {
        return ImportErrorItem.builder()
                .field("SYSTEM")
                .message(message)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeNoAccountDTO> getEmployeesWithoutAccount() {
        List<Employee> employees = employeeRepository.findAllEmployeesWithoutAccount();

        return employees.stream()
                .map(e -> EmployeeNoAccountDTO.builder()
                        .id(e.getId())
                        .employeeCode(e.getEmployeeCode())
                        .fullName(e.getFullName())
                        .teamName(e.getTeams() != null && !e.getTeams().isEmpty() ? e.getTeams().get(0).getName() : "N/A")
                        .status(e.getStatus().name())
                        .build())
                .collect(toList());
    }

    @Override
    @Transactional
    public void removeEmployeesFromTeam(Long teamId, List<Long> employeeId) {
        if (employeeId == null || employeeId.isEmpty())
            return;

        if (!teamRepository.existsById(teamId)) {
            throw new AppException(ErrorCode.TEAM_NOT_FOUND);
        }

        employeeRepository.removeEmployeesFromTeam(teamId, employeeId);
    }

    @Override
    @Transactional
    public void addEmployeesToTeam(Long teamId, List<Long> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty())
            return;

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND));

        List<Employee> employees = employeeRepository.findAllById(employeeIds);
        if (employees.size() != employeeIds.size()) {
            throw new AppException(ErrorCode.EMPLOYEE_NOT_FOUND);
        }

        for (Employee employee : employees) {
            boolean alreadyInTeam = employee.getTeams().stream()
                    .anyMatch(t -> t.getId().equals(teamId));
            if (!alreadyInTeam) {
                employee.getTeams().add(team);
            }
        }

        employeeRepository.saveAll(employees);
    }

    @Override
    public ProcessResponse getEmployeesByProcess(Long processId) {
        ProcessResponse processResponse = processRepository.findById(processId).map(processMapper::toDTO)
                .orElseThrow(() -> new AppException(ErrorCode.PROCESS_NOT_FOUND));

        processResponse.setEmployeeSkills(employeeSkillRepository.findByProcessIdAndDeleteFlagFalse(processId).stream()
                .map(employeeSkillMapper::toDto).toList());

        return processResponse;
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        if (request.getEmployeeCode() != null && !employee.getEmployeeCode().equals(request.getEmployeeCode())) {
            if (employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
                throw new AppException(ErrorCode.EMPLOYEE_CODE_ALREADY_LINKED);
            }
        }

        if (request.getTeamIds() != null && !request.getTeamIds().get(0).equals(employee.getTeams().get(0).getId())) {
            boolean isTeamExist = teamRepository.existsById(request.getTeamIds().get(0));
            if (!isTeamExist) {
                throw new AppException(ErrorCode.TEAM_NOT_FOUND);
            }
        }

        employeeMapper.updateEntity(employee, request);

        return employeeMapper.toDTO(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        employee.setDeleteFlag(true);
        employee.setStatus(EmployeeStatus.RESIGNED);

        employeeRepository.save(employee);
    }

    @Override
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .filter(e -> !e.isDeleteFlag())
                .map(this::enrichEmployeeData)
                .collect(toList());
    }

    @Override
    public List<EmployeeResponse> getEmployeesUnderManagement(User currentUser) {
        Set<Long> teamIds = new HashSet<>();

        if (currentUser != null) {
            if (SecurityUtils.hasPermission(currentUser, "section.manage")) {
                teamIds.addAll(teamRepository.findAllBySectionManagerId(currentUser.getId())
                        .stream().map(Team::getId).toList());
            }
            if (hasPermission(currentUser, "group.manage")) {
                teamIds.addAll(teamRepository.findAllByGroupSupervisorId(currentUser.getId())
                        .stream().map(Team::getId).toList());
            }
            if (hasPermission(currentUser, "team.manage")) {
                teamIds.addAll(teamRepository.findAllByTeamLeaderId(currentUser.getId())
                        .stream().map(Team::getId).toList());
            }
        }

        if (teamIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Employee> employees = employeeRepository.findAllByTeamIdIn(new ArrayList<>(teamIds));

        return employees.stream()
                .filter(e -> !e.isDeleteFlag())
                .map(this::enrichEmployeeData)
                .toList();
    }

    private EmployeeResponse enrichEmployeeData(Employee employee) {
        EmployeeResponse dto = employeeMapper.toDTO(employee);

        if (employee.getTeams() != null && !employee.getTeams().isEmpty()) {
            String teamNames = employee.getTeams().stream()
                    .map(Team::getName).collect(Collectors.joining(", "));
            dto.setTeamName(teamNames);

            Team firstTeam = employee.getTeams().get(0);
            dto.setGroupName(firstTeam.getGroup() != null ? firstTeam.getGroup().getName() : "N/A");
            dto.setSectionName(firstTeam.getGroup() != null && firstTeam.getGroup().getSection() != null
                    ? firstTeam.getGroup().getSection().getName()
                    : "N/A");
        }

        userRepository.findByEmployeeCodeWithRoles(employee.getEmployeeCode())
                .ifPresent(user -> dto.setRoles(user.getRoles().stream()
                        .map(Role::getDisplayName).toList()));

        List<TrainingResultDetail> results = trainingResultDetailRepository
                .findAllByEmployeeIdAndDeleteFlagFalse(employee.getId());

        int totalTraining = results.size();
        long totalFail = results.stream().filter(r -> Boolean.FALSE.equals(r.getIsPass())).count();

        dto.setTotalTraining(totalTraining);
        dto.setTotalFail((int) totalFail);

        return dto;
    }
}