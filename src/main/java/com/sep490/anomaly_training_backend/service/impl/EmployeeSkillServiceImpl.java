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
import com.sep490.anomaly_training_backend.service.EmployeeSkillService;
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

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;


    @Override
    public EmployeeSkillResponse createEmployeeSkill(EmployeeSkillRequest request) {

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        Process process = processRepository.findById(request.getProcessId())
                .orElseThrow(() -> new AppException(ErrorCode.PROCESS_NOT_FOUND));

        EmployeeSkill entity = EmployeeSkill.builder()
                .employee(employee)
                .process(process)
                .certifiedDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusYears(2))
                .build();

        EmployeeSkill saved = employeeSkillRepository.save(entity);

        return employeeSkillMapper.toDto(saved);
    }

    @Override
    public EmployeeSkillResponse updateEmployeeSkillByTeamLead(Long id, EmployeeSkillRequest request) {
        EmployeeSkill skill = employeeSkillRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_SKILL_NOT_FOUND));

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        Process process = processRepository.findById(request.getProcessId())
                .orElseThrow(() -> new AppException(ErrorCode.PROCESS_NOT_FOUND));

        skill.setEmployee(employee);
        skill.setProcess(process);
        skill.setStatus(request.getStatus());
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
                ? employeeRepository.findByTeamIdAndDeleteFlagFalse(teamId)
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

            List<EmployeeSkill> employeeSkillsList = skillsByEmployee.getOrDefault(employee.getId(), Collections.emptyList());
            Map<Long, SkillStatusDto> skillsMap = employeeSkillsList.stream()
                    .collect(Collectors.toMap(
                            skill -> skill.getProcess().getId(),
                            this::mapToSkillStatusDto
                    ));

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
    public void importSkillMatrix(MultipartFile file) {
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

            // Setup mối quan hệ với Team & Group cố định
            processParsedData(importSkillMatrixResult);

            log.info("Skill matrix import completed.");
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during import: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNEXPECTED_IMPORT_ERROR, "Unexpected error: " + e.getMessage());
        }
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
     * Process parsed data và setup mối quan hệ
     * Tất cả data thuộc về 1 Team cố định từ file header
     */
    private void processParsedData(ImportSkillMatrixResult importSkillMatrixResult) {
        processHierarchyMap(importSkillMatrixResult.getTeamCode(), importSkillMatrixResult.getGroupCode(), importSkillMatrixResult.getHierarchyMap());
        processParsedRawData(importSkillMatrixResult.getTeamCode(), importSkillMatrixResult.getGroupCode(), importSkillMatrixResult.getParsedRows());
    }

    private void processHierarchyMap(String teamCode, String groupCode, Map<String, Map<String, Set<String>>> hierarchyMap) {
        Team team = teamRepository.findByCode(teamCode)
                .orElse(new Team(teamCode));
        teamRepository.save(team);

        Group group = groupRepository.findByCode(groupCode)
                .orElse(new Group(groupCode));
        groupRepository.save(group);

        // Process hierarchy map
        for (Map.Entry<String, Map<String, Set<String>>> sectionEntry : hierarchyMap.entrySet()) {
            String sectionCode = sectionEntry.getKey();

            Section section = sectionRepository.findByCode(sectionCode)
                    .orElse(new Section(sectionCode));
            sectionRepository.save(section);

            if (group.getSection() == null) {
                group.setSection(section);
            }

            for (Map.Entry<String, Set<String>> plEntry : sectionEntry.getValue().entrySet()) {
                String productLineCode = plEntry.getKey();

                ProductLine productLine = productLineRepository.findByCode(productLineCode)
                        .orElseGet(() -> {
                            ProductLine newPL = ProductLine.builder()
                                    .code(productLineCode)
                                    .group(group)
                                    .build();
                            return productLineRepository.save(newPL);
                        });

                for (String processName : plEntry.getValue()) {
                    Process process = processRepository.findByName(processName)
                            .orElseGet(() -> {
                                Process newProcess = Process.builder()
                                        .name(processName)
                                        .productLine(productLine)
                                        .build();
                                return processRepository.save(newProcess);
                            });

                    log.info("Processed: Team={}, Group={}, Section={}, ProductLine={}, Process={}",
                            team.getCode(), group.getCode(), section.getCode(), productLine.getCode(), process.getCode());
                }
            }
        }
    }

    private void processParsedRawData(String teamCode, String groupCode, List<EmployeeSkillCertificationImportDto> parsedResults) {
        for (EmployeeSkillCertificationImportDto raw : parsedResults) {
            Employee employee = employeeRepository.findByEmployeeCode(raw.getEmployeeId())
                    .orElseGet(() -> {
                        Employee newEmployee = Employee.builder()
                                .employeeCode(raw.getEmployeeId())
                                .fullName(raw.getEmployeeFullName())
                                .team(teamRepository.findByCode(teamCode)
                                        .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND, "Team not found: " + teamCode)))
                                .build();
                        return employeeRepository.save(newEmployee);
                    });

            Process process = processRepository.findByName(raw.getProcessName())
                    .orElseThrow(() -> new AppException(ErrorCode.PROCESS_NOT_FOUND, "Process not found: " + raw.getProcessName()));

            EmployeeSkill skill = EmployeeSkill.builder()
                    .employee(employee)
                    .process(process)
                    .certifiedDate(raw.getCertificationDate())
                    .status(EmployeeSkillStatus.VALID)
                    .build();

            employeeSkillRepository.save(skill);

            log.info("Saved skill for employee {} on process {}", employee.getEmployeeCode(), process.getCode());
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
