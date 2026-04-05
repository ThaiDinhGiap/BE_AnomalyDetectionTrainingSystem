package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.EmployeeSkillCertificationImportDto;
import com.sep490.anomaly_training_backend.dto.ImportSkillMatrixResult;
import com.sep490.anomaly_training_backend.dto.request.EmployeeSkillRequest;
import com.sep490.anomaly_training_backend.dto.response.EmployeeSkillResponse;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import com.sep490.anomaly_training_backend.dto.response.skill_matrix.EmployeeSkillsDto;
import com.sep490.anomaly_training_backend.dto.response.skill_matrix.ProcessCompletionDto;
import com.sep490.anomaly_training_backend.dto.response.skill_matrix.SkillMatrixResponse;
import com.sep490.anomaly_training_backend.dto.response.skill_matrix.SkillStatusDto;
import com.sep490.anomaly_training_backend.enums.EmployeeSkillStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.EmployeeSkillMapper;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.EmployeeSkill;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.Section;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.repository.EmployeeRepository;
import com.sep490.anomaly_training_backend.repository.EmployeeSkillRepository;
import com.sep490.anomaly_training_backend.repository.GroupRepository;
import com.sep490.anomaly_training_backend.repository.ProcessRepository;
import com.sep490.anomaly_training_backend.repository.ProductLineRepository;
import com.sep490.anomaly_training_backend.repository.SectionRepository;
import com.sep490.anomaly_training_backend.repository.TeamRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultDetailRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.EmployeeSkillService;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.util.helper.EmployeeSkillCertificationImportHelper;
import com.sep490.anomaly_training_backend.util.validator.EmployeeSkillCertificationImportValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeSkillServiceImpl implements EmployeeSkillService {

    private final EmployeeRepository employeeRepository;
    private final ProcessRepository processRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final TrainingResultDetailRepository trainingResultDetailRepository;
    private final EmployeeSkillMapper employeeSkillMapper;

    private final EmployeeSkillCertificationImportHelper importHelper;
    private final EmployeeSkillCertificationImportValidator importValidator;
    private final ProductLineRepository productLineRepository;
    private final SectionRepository sectionRepository;
    private final TeamRepository teamRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Override
    public EmployeeSkillResponse createEmployeeSkill(EmployeeSkillRequest request) {

        Employee employee;
        if (request.getEmployeeId() != null) {
            employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        } else if (request.getEmployeeCode() != null) {
            employee = employeeRepository.findByEmployeeCode(request.getEmployeeCode())
                    .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        } else {
            throw new AppException(ErrorCode.EMPLOYEE_NOT_FOUND);
        }

        Process process = processRepository.findById(request.getProcessId())
                .orElseThrow(() -> new AppException(ErrorCode.PROCESS_NOT_FOUND));

        LocalDate certifiedDate = request.getCertifiedDate() != null ? request.getCertifiedDate() : LocalDate.now();
        LocalDate expiryDate = request.getExpiryDate() != null ? request.getExpiryDate() : certifiedDate.plusYears(2);

        EmployeeSkill entity = EmployeeSkill.builder()
                .employee(employee)
                .process(process)
                .certifiedDate(certifiedDate)
                .expiryDate(expiryDate)
                .build();

        EmployeeSkill saved = employeeSkillRepository.save(entity);

        return employeeSkillMapper.toDto(saved);
    }

    @Override
    public EmployeeSkillResponse updateEmployeeSkillByTeamLead(Long id, EmployeeSkillRequest request) {
        EmployeeSkill skill = employeeSkillRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_SKILL_NOT_FOUND));

        if (request.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
            skill.setEmployee(employee);
        }
        if (request.getProcessId() != null) {
            Process process = processRepository.findById(request.getProcessId())
                    .orElseThrow(() -> new AppException(ErrorCode.PROCESS_NOT_FOUND));
            skill.setProcess(process);
        }
        if (request.getStatus() != null) {
            skill.setStatus(request.getStatus());
        }
        if (request.getCertifiedDate() != null) {
            skill.setCertifiedDate(request.getCertifiedDate());
        }
        if (request.getExpiryDate() != null) {
            skill.setExpiryDate(request.getExpiryDate());
        }
        return employeeSkillMapper.toDto(employeeSkillRepository.save(skill));
    }

    @Override
    public void deleteEmployeeSkill(Long id) {
        EmployeeSkill skill = employeeSkillRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_SKILL_NOT_FOUND));
        skill.setDeleteFlag(true);
        employeeSkillRepository.save(skill);
    }

    @Override
    public SkillMatrixResponse getSkillMatrix(Long teamId, Long lineId, List<Long> employeeIds, List<Long> processIds) {
        // 1. Get processes for the header, filtering if processIds are provided
        List<Process> processModels = (CollectionUtils.isEmpty(processIds))
                ? processRepository.findByProductLineIdAndDeleteFlagFalse(lineId)
                : processRepository.findAllById(processIds);

        // 2. Get employees for the rows, filtering if employeeIds are provided
        List<Employee> employees = (CollectionUtils.isEmpty(employeeIds))
                ? employeeRepository.findByTeamsIdAndDeleteFlagFalse(teamId)
                : employeeRepository.findAllById(employeeIds);
        List<Long> finalEmployeeIds = employees.stream().map(Employee::getId).collect(Collectors.toList());

        // 3. Get all relevant skills and training details in bulk
        Map<Long, List<EmployeeSkill>> skillsByEmployee = employeeSkillRepository.findByEmployeeIdIn(finalEmployeeIds)
                .stream().collect(Collectors.groupingBy(skill -> skill.getEmployee().getId()));

        List<Long> pIds = processModels.stream().map(Process::getId).collect(Collectors.toList());
        Map<Long, Long> completedCountByProcess = trainingResultDetailRepository.countCompletedByProcessIds(pIds)
                .stream().collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
        Map<Long, Long> totalCountByProcess = trainingResultDetailRepository.countTotalByProcessIds(pIds)
                .stream().collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

        // 4. Build Process DTOs with completion rate
        List<ProcessCompletionDto> processDtos = processModels.stream().map(p -> {
            ProcessCompletionDto dto = new ProcessCompletionDto(p.getId(), p.getCode(), p.getName());
            long completed = completedCountByProcess.getOrDefault(p.getId(), 0L);
            long total = totalCountByProcess.getOrDefault(p.getId(), 0L);
            dto.setCompletionRate(completed + "/" + total);
            return dto;
        }).collect(Collectors.toList());

        // 5. Build the matrix data
        List<EmployeeSkillsDto> employeeSkillsDtos = employees.stream().map(employee -> {
            EmployeeSkillsDto dto = new EmployeeSkillsDto();
            dto.setEmployeeId(employee.getId());
            dto.setEmployeeName(employee.getFullName());
            dto.setEmployeeCode(employee.getEmployeeCode());

            List<EmployeeSkill> employeeSkillsList = skillsByEmployee.getOrDefault(employee.getId(),
                    Collections.emptyList());
            Map<Long, SkillStatusDto> skillsMap = employeeSkillsList.stream()
                    .collect(Collectors.toMap(
                            skill -> skill.getProcess().getId(),
                            this::mapToSkillStatusDto));

            // Fill in missing skills with "NONE" status
            processModels.forEach(process -> {
                skillsMap.putIfAbsent(process.getId(), SkillStatusDto.builder().status("NONE").build());
            });

            dto.setSkills(skillsMap);
            return dto;
        }).collect(Collectors.toList());

        SkillMatrixResponse response = new SkillMatrixResponse();
        response.setProcesses(processDtos);
        response.setEmployeeSkills(employeeSkillsDtos);
        return response;
    }

    @Override
    @Transactional
    public void importSkillMatrix(MultipartFile file, User currentUser) {
        log.info("Starting skill matrix import from file: {}", file.getOriginalFilename());

        try {
            validateFile(file);

            List<ImportErrorItem> errors = new ArrayList<>();
            ImportSkillMatrixResult importSkillMatrixResult = parseExcelFileWithHierarchy(file, errors);

            importValidator.validateFileData(importSkillMatrixResult.getParsedRows(), errors);

            if (!errors.isEmpty()) {
                log.error("Validation errors found: {}", errors.size());
                throw buildAppException(errors);
            }

            // Validate Manager & Supervisor exist in DB
            User manager = userRepository.findByEmployeeCodeAndDeleteFlagFalse(
                    importSkillMatrixResult.getManagerCode())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND,
                            "Manager not found with employee code: " + importSkillMatrixResult.getManagerCode()));

            User supervisor = userRepository.findByEmployeeCodeAndDeleteFlagFalse(
                    importSkillMatrixResult.getSupervisorCode())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND,
                            "Supervisor not found with employee code: " + importSkillMatrixResult.getSupervisorCode()));

            // Setup mối quan hệ với Team & Group cố định
            processParsedData(importSkillMatrixResult, manager, supervisor, currentUser);

            log.info("Skill matrix import completed.");
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during import: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNEXPECTED_IMPORT_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    @Override
    public List<EmployeeSkillResponse> getEmployeeSkillsByEmployeeId(Long employeeId) {
        return employeeSkillRepository.findByEmployeeIdAndDeleteFlagFalse(employeeId)
                .stream()
                .map(employeeSkillMapper::toDto)
                .toList();
    }

    private ImportSkillMatrixResult parseExcelFileWithHierarchy(
            MultipartFile file,
            List<ImportErrorItem> errors) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new AppException(ErrorCode.EXCEL_SHEET_NOT_FOUND);
            }

            return importHelper.parseExcelRowsWithHierarchy(sheet, errors);
        } catch (IOException e) {
            log.error("Error reading Excel file: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.CANNOT_READ_EXCEL_FILE, "Cannot read Excel file: " + e.getMessage());
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error parsing Excel rows: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.IMPORT_PARSE_ERROR, "Error parsing rows: " + e.getMessage());
        }
    }

    /**
     * Process parsed data and setup relationships
     * All data belongs to a single Team from file header
     */
    private void processParsedData(ImportSkillMatrixResult importSkillMatrixResult,
                                    User manager, User supervisor, User currentUser) {
        processHierarchyMap(importSkillMatrixResult, manager, supervisor, currentUser);
        processParsedRawData(importSkillMatrixResult.getTeamCode(), importSkillMatrixResult.getGroupCode(),
                importSkillMatrixResult.getParsedRows());
    }

    private void processHierarchyMap(ImportSkillMatrixResult result,
            User manager, User supervisor, User currentUser) {

        String teamCode = result.getTeamCode();
        String teamName = result.getTeamName();
        String groupCode = result.getGroupCode();
        String groupName = result.getGroupName();
        String sectionCode = result.getSectionCode();
        String sectionName = result.getSectionName();
        String lineCode = result.getLineCode();
        String lineName = result.getLineName();

        // Step 1: Resolve Section (find or create, update name)
        Section section = sectionRepository.findByCode(sectionCode)
                .orElseGet(() -> { Section s = new Section(sectionCode); s.setName(sectionName); return s; });
        if (sectionName != null && !sectionName.isBlank()) {
            section.setName(sectionName);
        }
        section.setManager(manager);
        section = sectionRepository.save(section);

        // Step 2: Resolve Group (find or create, update name)
        Group group = groupRepository.findByCode(groupCode)
                .orElseGet(() -> { Group g = new Group(groupCode); g.setName(groupName); return g; });
        if (groupName != null && !groupName.isBlank()) {
            group.setName(groupName);
        }
        group.setSupervisor(supervisor);
        group.setSection(section);
        groupRepository.save(group);

        // Step 3: Resolve Team (find or create, update name)
        Team team = teamRepository.findByCode(teamCode)
                .orElseGet(() -> { Team t = new Team(teamCode); t.setName(teamName); return t; });
        if (teamName != null && !teamName.isBlank()) {
            team.setName(teamName);
        }
        team.setGroup(group);
        team.setTeamLeader(currentUser);
        teamRepository.save(team);

        // Step 4: Resolve ProductLine from header "Line" (find or create, update name)
        ProductLine headerProductLine = productLineRepository.findByCode(lineCode)
                .orElseGet(() -> {
                    ProductLine newPL = ProductLine.builder()
                            .code(lineCode)
                            .name(lineName)
                            .group(group)
                            .build();
                    return productLineRepository.save(newPL);
                });
        if (lineName != null && !lineName.isBlank()) {
            headerProductLine.setName(lineName);
        }
        headerProductLine.setGroup(group);
        productLineRepository.save(headerProductLine);

        // Step 5: Process hierarchy map entries (Section → ProductLine → Process)
        Map<String, Map<String, Set<String>>> hierarchyMap = result.getHierarchyMap();
        for (Map.Entry<String, Map<String, Set<String>>> sectionEntry : hierarchyMap.entrySet()) {
            for (Map.Entry<String, Set<String>> plEntry : sectionEntry.getValue().entrySet()) {
                String productLineCode = plEntry.getKey();

                ProductLine productLine = productLineRepository.findByCode(productLineCode)
                        .orElseGet(() -> {
                            ProductLine newPL = ProductLine.builder()
                                    .code(productLineCode)
                                    .name(productLineCode)
                                    .group(group)
                                    .build();
                            return productLineRepository.save(newPL);
                        });

                for (String processCode : plEntry.getValue()) {
                    // Find process by code, create if not found
                    processRepository.findByCode(processCode)
                            .orElseGet(() -> {
                                Process newProcess = Process.builder()
                                        .code(processCode)
                                        .name(processCode) // name defaults to code; will be updated by row data
                                        .productLine(productLine)
                                        .build();
                                return processRepository.save(newProcess);
                            });
                }
            }
        }

        log.info("Hierarchy established: Section={} ({}), Group={} ({}), Team={} ({}), Line={} ({})",
                sectionCode, sectionName, groupCode, groupName, teamCode, teamName, lineCode, lineName);
    }

    private void processParsedRawData(String teamCode, String groupCode,
            List<EmployeeSkillCertificationImportDto> parsedResults) {
        // Resolve team once for all employees
        Team team = teamRepository.findByCode(teamCode)
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND,
                        "Team not found: " + teamCode));

        // Track already-processed employees to avoid redundant team checks
        Set<String> processedEmployeeCodes = new java.util.HashSet<>();

        for (EmployeeSkillCertificationImportDto raw : parsedResults) {
            Employee employee = employeeRepository.findByEmployeeCode(raw.getEmployeeId())
                    .orElseGet(() -> {
                        Employee newEmployee = Employee.builder()
                                .employeeCode(raw.getEmployeeId())
                                .fullName(raw.getEmployeeFullName())
                                .build();
                        return employeeRepository.save(newEmployee);
                    });

            // Assign team if not already assigned
            if (processedEmployeeCodes.add(employee.getEmployeeCode())) {
                boolean alreadyInTeam = employee.getTeams() != null
                        && employee.getTeams().stream()
                                .anyMatch(t -> t.getId().equals(team.getId()));
                if (!alreadyInTeam) {
                    if (employee.getTeams() == null) {
                        employee.setTeams(new ArrayList<>());
                    }
                    employee.getTeams().add(team);
                    employee = employeeRepository.save(employee);
                    log.info("Assigned employee {} to team {}", employee.getEmployeeCode(), team.getCode());
                }
            }

            // Resolve process by CODE instead of name
            Process process = processRepository.findByCode(raw.getProcessCode())
                    .orElseThrow(() -> new AppException(ErrorCode.PROCESS_NOT_FOUND,
                            "Process not found with code: " + raw.getProcessCode()));

            // Update process name if provided and different
            if (raw.getProcessName() != null && !raw.getProcessName().isBlank()
                    && !raw.getProcessName().equals(process.getName())) {
                process.setName(raw.getProcessName());
                processRepository.save(process);
            }

            // Upsert: skip if duplicate (employee + process), or update certifiedDate
            EmployeeSkill skill = employeeSkillRepository
                    .findByEmployeeIdAndProcessIdAndDeleteFlagFalse(employee.getId(), process.getId())
                    .orElse(null);

            if (skill != null) {
                if (raw.getCertificationDate() != null
                        && (skill.getCertifiedDate() == null
                            || raw.getCertificationDate().isAfter(skill.getCertifiedDate()))) {
                    skill.setCertifiedDate(raw.getCertificationDate());
                    employeeSkillRepository.save(skill);
                    log.info("Updated existing skill for employee {} on process {}",
                            employee.getEmployeeCode(), process.getCode());
                } else {
                    log.info("Skipped duplicate skill for employee {} on process {}",
                            employee.getEmployeeCode(), process.getCode());
                }
            } else {
                EmployeeSkill newSkill = EmployeeSkill.builder()
                        .employee(employee)
                        .process(process)
                        .certifiedDate(raw.getCertificationDate())
                        .status(EmployeeSkillStatus.VALID)
                        .build();
                employeeSkillRepository.save(newSkill);
                log.info("Saved new skill for employee {} on process {}",
                        employee.getEmployeeCode(), process.getCode());
            }
        }
    }

    /**
     * Validate file format
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_IS_EMPTY);
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new AppException(ErrorCode.INVALID_FILE_FORMAT);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.FILE_SIZE_EXCEEDS_LIMIT);
        }
    }

    /**
     * Build AppException from error list
     */
    private AppException buildAppException(List<ImportErrorItem> errors) {
        StringBuilder message = new StringBuilder("Import validation failed:\n");
        for (ImportErrorItem error : errors) {
            message.append("Row ").append(error.getRowNumber())
                    .append(" (").append(error.getField()).append(")")
                    .append(": ").append(error.getMessage()).append("\n");
        }
        return new AppException(ErrorCode.IMPORT_VALIDATION_ERROR, message.toString());
    }

    private SkillStatusDto mapToSkillStatusDto(EmployeeSkill skill) {
        String status;
        LocalDate expiryDate = skill.getExpiryDate();
        LocalDate today = LocalDate.now();
        LocalDate ninetyDaysFromNow = today.plusDays(90);

        switch (skill.getStatus()) {
            case VALID:
                if (expiryDate != null) {
                    if (expiryDate.isBefore(today)) {
                        status = "EXPIRED";
                    } else if (expiryDate.isBefore(ninetyDaysFromNow)) {
                        status = "WARNING";
                    } else {
                        status = "PASS";
                    }
                } else {
                    status = "PASS";
                }
                break;
            case PENDING_REVIEW:
                status = "PENDING";
                break;
            case REVOKED:
                status = "REVOKED";
                break;
            default:
                status = "NONE";
                break;
        }

        return SkillStatusDto.builder()
                .status(status)
                .expiryDate(expiryDate)
                .build();
    }
}
