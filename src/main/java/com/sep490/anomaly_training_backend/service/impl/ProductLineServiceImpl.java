package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.ProductLineImportDto;
import com.sep490.anomaly_training_backend.dto.request.ProductLineRequest;
import com.sep490.anomaly_training_backend.dto.response.EmployeeSkillResponse;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import com.sep490.anomaly_training_backend.dto.response.OrgDropdownItem;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.dto.response.ProductLineDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.ProductLineResponse;
import com.sep490.anomaly_training_backend.dto.response.WorkingPosition;
import com.sep490.anomaly_training_backend.enums.ImportStatus;
import com.sep490.anomaly_training_backend.enums.ImportType;
import com.sep490.anomaly_training_backend.enums.OrgHierarchyLevel;
import com.sep490.anomaly_training_backend.enums.ProcessClassification;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.EmployeeSkillMapper;
import com.sep490.anomaly_training_backend.mapper.ProcessMapper;
import com.sep490.anomaly_training_backend.mapper.ProductLineMapper;
import com.sep490.anomaly_training_backend.model.Attachment;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.model.Product;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.ProductProcess;
import com.sep490.anomaly_training_backend.model.Section;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.EmployeeSkillRepository;
import com.sep490.anomaly_training_backend.repository.GroupRepository;
import com.sep490.anomaly_training_backend.repository.ProcessRepository;
import com.sep490.anomaly_training_backend.repository.ProductLineRepository;
import com.sep490.anomaly_training_backend.repository.ProductProcessRepository;
import com.sep490.anomaly_training_backend.repository.SectionRepository;
import com.sep490.anomaly_training_backend.repository.TeamRepository;
import com.sep490.anomaly_training_backend.service.ImportHistoryService;
import com.sep490.anomaly_training_backend.service.ProductLineService;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import com.sep490.anomaly_training_backend.util.helper.ProductLineImportHelper;
import com.sep490.anomaly_training_backend.util.validator.ProductLineImportValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductLineServiceImpl implements ProductLineService {
    private final ProductLineRepository productLineRepository;
    private final ProcessRepository processRepository;
    private final ProductLineMapper productLineMapper;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final EmployeeSkillMapper employeeSkillMapper;
    private final GroupRepository groupRepository;
    private final ImportHistoryService importHistoryService;
    private final ProductLineImportHelper importHelper;
    private final ProductLineImportValidator importValidator;
    private final ProcessMapper processMapper;
    private final TeamRepository teamRepository;
    private final SectionRepository sectionRepository;
    private final ProductProcessRepository productProcessRepository;
    private final AttachmentService attachmentService;

    @Override
    public List<ProductLineResponse> getAllProductLine() {
        List<ProductLineResponse> responses = productLineRepository.findByDeleteFlagFalse()
                .stream().map(productLineMapper::toDto)
                .toList();
        for (ProductLineResponse response : responses) {
            for (ProcessResponse processResponse : response.getProcesses()) {
                List<EmployeeSkillResponse> skillResponses = employeeSkillRepository
                        .findByProcessIdAndDeleteFlagFalse(processResponse.getId())
                        .stream().map(employeeSkillMapper::toDto).toList();
                processResponse.setEmployeeSkills(skillResponses);
            }
        }
        return responses;
    }

    @Override
    public ProductLineResponse getProductLineDetail(Long productLineId) {
        ProductLine entity = productLineRepository.findById(productLineId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_LINE_NOT_FOUND));
        return productLineMapper.toDto(entity);
    }

    @Override
    public ProductLineResponse createProductLine(ProductLineRequest request) {
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

        if (request.getCode() != null && productLineRepository.findByCode(request.getCode()).isPresent()) {
            throw new AppException(ErrorCode.PRODUCT_LINE_CODE_ALREADY_EXISTS);
        }

        ProductLine productLine = ProductLine.builder()
                .code(request.getCode())
                .name(request.getName())
                .group(group)
                .build();

        return productLineMapper.toDto(productLineRepository.save(productLine));
    }

    @Override
    public void deleteProductLine(Long id) {
        ProductLine productLine = productLineRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_LINE_NOT_FOUND));
        productLine.setDeleteFlag(true);
        productLineRepository.saveAndFlush(productLine);
    }

    @Override
    public ProductLineResponse updateProductLine(Long id, ProductLineRequest productLineRequest) {
        ProductLine productLine = productLineRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_LINE_NOT_FOUND));

        if (productLineRequest.getName() != null) {
            productLine.setName(productLineRequest.getName());
        }
        if (productLineRequest.getGroupId() != null) {
            Group group = groupRepository.findById(productLineRequest.getGroupId())
                    .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));
            productLine.setGroup(group);
        }

        return productLineMapper.toDto(productLineRepository.save(productLine));
    }

    @Override
    public List<ProductLineResponse> getByTeamLeadId(Long teamLeadId) {
        return productLineRepository.findProductLineByTeamLeadId(teamLeadId).stream()
                .map(productLineMapper::toDto)
                .toList();
    }

    @Override
    public List<ProductLineResponse> importProductLine(User user, MultipartFile productFile) {
        List<ImportErrorItem> errors = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(productFile.getInputStream())) {
            validateImportFile(productFile);
            Sheet sheet = getFirstSheet(workbook);

            // Step 1: Parse rows with merge cell handling
            List<ProductLineImportDto> parsedRows = importHelper.parseExcelRows(sheet, errors);

            if (!errors.isEmpty()) {
                saveImportFailHistory(user, productFile, errors);
                throw new AppException(ErrorCode.IMPORT_PARSE_ERROR);
            }

            // Step 2: Validate file data (NO DB check yet)
            importValidator.validateFileData(parsedRows, errors);

            if (!errors.isEmpty()) {
                saveImportFailHistory(user, productFile, errors);
                throw new AppException(ErrorCode.IMPORT_VALIDATION_ERROR);
            }

            // Step 3: Process all rows - create/update ProductLine and Process
            List<ProductLineResponse> responses = processAllRows(parsedRows, errors);

            // Step 4: If any errors occurred during processing, save them
            if (!errors.isEmpty()) {
                saveImportFailHistory(user, productFile, errors);
                throw new AppException(ErrorCode.IMPORT_FAILED);
            }

            // Step 5: Save success history
            saveImportPassHistory(user, productFile);

            return responses;

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Import product line failed", e);
            if (errors.isEmpty()) {
                errors.add(buildSystemError("System error: " + e.getMessage()));
                saveImportFailHistory(user, productFile, errors);
            }
            throw new AppException(ErrorCode.CANNOT_READ_EXCEL_FILE);
        }
    }

    @Override
    public List<WorkingPosition> getWorkingPosition(User user) {
        if (user.hasPermission("team.manage")) {
            return positionTeamLead(user);
        } else if ((user.hasPermission("group.manage"))) {
            return positionSuperVisor(user);
        } else if ((user.hasPermission("group.admin"))) {
            return positionManager(user);
        }

        return null;
    }

    // ==================== CASCADING ORG HIERARCHY ====================

    @Override
    @Transactional(readOnly = true)
    public List<OrgDropdownItem> getOrgHierarchy(User user, OrgHierarchyLevel level, Long sectionId, Long groupId) {
        return switch (level) {
            case SECTION -> getSectionsForUser(user);
            case GROUP -> getGroupsForUser(user, sectionId);
            case TEAM -> getTeamsForUser(user, groupId);
            case PRODUCT_LINE -> getProductLinesForUser(user, groupId);
        };
    }

    private List<OrgDropdownItem> getSectionsForUser(User user) {
        Set<Section> sections = new LinkedHashSet<>();

        if (user.hasPermission("section.manage")) {
            sections.addAll(sectionRepository.findByManagerId(user.getId()));
        }
        if (user.hasPermission("group.manage")) {
            groupRepository.findBySupervisorId(user.getId()).stream()
                    .map(Group::getSection)
                    .filter(Objects::nonNull)
                    .forEach(sections::add);
        }
        if (user.hasPermission("team.manage")) {
            teamRepository.findAllByTeamLeaderId(user.getId()).stream()
                    .map(Team::getGroup)
                    .filter(Objects::nonNull)
                    .map(Group::getSection)
                    .filter(Objects::nonNull)
                    .forEach(sections::add);
        }

        return sections.stream()
                .map(s ->
                        OrgDropdownItem.builder()
                                .id(s.getId())
                                .name(s.getName())
                                .code(s.getCode())
                                .fullName(s.getManager().getFullName())
                                .employeeCode(s.getManager().getEmployeeCode())
                                .build())
                .collect(Collectors.toList());
    }

    private List<OrgDropdownItem> getGroupsForUser(User user, Long sectionId) {
        if (sectionId == null) return List.of();

        List<Group> allGroups = groupRepository.findBySectionIdAndDeleteFlagFalse(sectionId);

        Set<Long> allowedGroupIds = new LinkedHashSet<>();

        if (user.hasPermission("section.manage")) {
            sectionRepository.findByManagerId(user.getId()).stream()
                    .filter(s -> s.getId().equals(sectionId))
                    .findFirst()
                    .ifPresent(s -> allGroups.forEach(g -> allowedGroupIds.add(g.getId())));
        }
        if (user.hasPermission("group.manage")) {
            groupRepository.findBySupervisorId(user.getId()).stream()
                    .filter(g -> g.getSection() != null && g.getSection().getId().equals(sectionId))
                    .forEach(g -> allowedGroupIds.add(g.getId()));
        }
        if (user.hasPermission("team.manage")) {
            teamRepository.findAllByTeamLeaderId(user.getId()).stream()
                    .map(Team::getGroup)
                    .filter(g -> g != null && g.getSection() != null && g.getSection().getId().equals(sectionId))
                    .forEach(g -> allowedGroupIds.add(g.getId()));
        }

        return allGroups.stream()
                .filter(g -> allowedGroupIds.contains(g.getId()))
                .map(g ->
                        OrgDropdownItem.builder()
                                .id(g.getId())
                                .name(g.getName())
                                .code(g.getCode())
                                .fullName(g.getSupervisor().getFullName())
                                .employeeCode(g.getSupervisor().getEmployeeCode())
                                .build())
                .collect(Collectors.toList());
    }

    private List<OrgDropdownItem> getTeamsForUser(User user, Long groupId) {
        if (groupId == null) return List.of();

        List<Team> allTeams = teamRepository.findByGroupId(groupId);

        Set<Long> allowedTeamIds = new LinkedHashSet<>();

        if (user.hasPermission("group.manage") || user.hasPermission("section.manage")) {
            allTeams.forEach(t -> allowedTeamIds.add(t.getId()));
        }
        if (user.hasPermission("team.manage")) {
            teamRepository.findAllByTeamLeaderId(user.getId()).stream()
                    .filter(t -> t.getGroup() != null && t.getGroup().getId().equals(groupId))
                    .forEach(t -> allowedTeamIds.add(t.getId()));
        }

        return allTeams.stream()
                .filter(t -> allowedTeamIds.contains(t.getId()))
                .map(t ->
                        OrgDropdownItem.builder()
                                .id(t.getId())
                                .name(t.getName())
                                .code(t.getCode())
                                .fullName(t.getTeamLeader().getFullName())
                                .employeeCode(t.getTeamLeader().getEmployeeCode())
                                .build())
                .collect(Collectors.toList());
    }

    private List<OrgDropdownItem> getProductLinesForUser(User user, Long groupId) {
        if (groupId == null) return List.of();

        return productLineRepository.findByGroupIdAndDeleteFlagFalse(groupId).stream()
                .map(pl ->
                        OrgDropdownItem.builder()
                                .id(pl.getId())
                                .name(pl.getName())
                                .code(pl.getCode())
                                .build())
                .collect(Collectors.toList());
    }

    private List<WorkingPosition> positionTeamLead(User user) {
        List<WorkingPosition> resultTeamLead = new ArrayList<>();
        List<Team> teams = teamRepository.findAllByTeamLeaderId(user.getId());
        for (Team team : teams) {
            Group group = team.getGroup();
            List<ProductLine> productLine = productLineRepository.findByGroupId(group.getId());
            for (ProductLine pl : productLine) {
                WorkingPosition workingPosition = new WorkingPosition();
                workingPosition.setProductLineId(pl.getId());
                workingPosition.setProductLineName(pl.getName());
                workingPosition.setGroupId(group.getId());
                workingPosition.setGroupName(group.getName());
                workingPosition.setSupervisorId(group.getSupervisor().getId());
                workingPosition.setSupervisorName(group.getSupervisor().getFullName());
                workingPosition.setSupervisorCode(group.getSupervisor().getEmployeeCode());
                workingPosition.setTeamId(team.getId());
                workingPosition.setTeamName(team.getName());
                workingPosition.setTeamLeadCode(team.getTeamLeader().getEmployeeCode());
                workingPosition.setTeamLeadName(team.getTeamLeader().getFullName());
                workingPosition.setFinalInspectionCode(team.getFinalInspection().getEmployeeCode());
                workingPosition.setFinalInspectionName(team.getFinalInspection().getFullName());
                workingPosition.setSectionId(group.getSection().getId());
                workingPosition.setSectionName(group.getSection().getName());
                workingPosition.setManagerId(group.getSection().getManager().getId());
                workingPosition.setManagerName(group.getSection().getManager().getFullName());
                workingPosition.setManagerCode(group.getSection().getManager().getEmployeeCode());
                resultTeamLead.add(workingPosition);
            }
        }
        return resultTeamLead;
    }

    private List<WorkingPosition> positionSuperVisor(User user) {
        List<WorkingPosition> resultSupervisor = new ArrayList<>();
        List<Group> groups = groupRepository.findBySupervisorId(user.getId());
        for (Group group : groups) {
            List<ProductLine> productLine = productLineRepository.findByGroupId(group.getId());
            for (ProductLine pl : productLine) {
                WorkingPosition workingPosition = new WorkingPosition();
                workingPosition.setProductLineId(pl.getId());
                workingPosition.setProductLineName(pl.getName());
                workingPosition.setGroupId(group.getId());
                workingPosition.setGroupName(group.getName());
                workingPosition.setSupervisorId(group.getSupervisor().getId());
                workingPosition.setSupervisorName(group.getSupervisor().getFullName());
                workingPosition.setSupervisorCode(group.getSupervisor().getEmployeeCode());
                workingPosition.setSectionId(group.getSection().getId());
                workingPosition.setSectionName(group.getSection().getName());
                workingPosition.setManagerId(group.getSection().getManager().getId());
                workingPosition.setManagerName(group.getSection().getManager().getFullName());
                workingPosition.setManagerCode(group.getSection().getManager().getEmployeeCode());
                resultSupervisor.add(workingPosition);
            }
        }
        return resultSupervisor;
    }

    private List<WorkingPosition> positionManager(User user) {
        List<WorkingPosition> resulManager = new ArrayList<>();
        List<Section> sections = sectionRepository.findByManagerId(user.getId());
        for (Section section : sections) {
            List<ProductLine> productLine = productLineRepository.findBySection(section.getId());
            for (ProductLine pl : productLine) {
                WorkingPosition workingPosition = new WorkingPosition();
                workingPosition.setProductLineId(pl.getId());
                workingPosition.setProductLineName(pl.getName());
                workingPosition.setSectionId(section.getId());
                workingPosition.setSectionName(section.getName());
                workingPosition.setManagerId(section.getManager().getId());
                workingPosition.setManagerName(section.getManager().getFullName());
                workingPosition.setManagerCode(section.getManager().getEmployeeCode());
                resulManager.add(workingPosition);
            }
        }
        return resulManager;
    }

    /**
     * Process all rows - group by ProductLine, then create/update
     */
    private List<ProductLineResponse> processAllRows(
            List<ProductLineImportDto> parsedRows,
            List<ImportErrorItem> errors) {

        List<ProductLineResponse> responses = new ArrayList<>();

        // Group rows by ProductLineCode to process ProductLine + Processes together
        Map<String, List<ProductLineImportDto>> groupedByProductLine = new HashMap<>();
        for (ProductLineImportDto dto : parsedRows) {
            groupedByProductLine
                    .computeIfAbsent(dto.getProductLineCode(), k -> new ArrayList<>())
                    .add(dto);
        }

        // Process each ProductLine group
        for (Map.Entry<String, List<ProductLineImportDto>> entry : groupedByProductLine.entrySet()) {
            try {
                String productLineCode = entry.getKey();
                List<ProductLineImportDto> processRows = entry.getValue();

                // Get first row info (they all have same ProductLine info)
                ProductLineImportDto firstRow = processRows.get(0);
                String productLineName = firstRow.getProductLineName();

                // Find or create ProductLine
                ProductLine productLine = findOrCreateProductLine(productLineCode, productLineName, errors);
                if (productLine == null) {
                    continue;
                }
                List<ProcessResponse> processResponses = new ArrayList<>();
                // Process all processes in this ProductLine
                for (ProductLineImportDto processDto : processRows) {
                    try {
                        Process process = findOrCreateProcess(productLine, processDto, errors);
                        if (process != null) {
                            processResponses.add(processMapper.toDTO(process));
                        }
                    } catch (Exception e) {
                        log.error("Error processing row {}: {}", processDto.getExcelRowNumber(), e.getMessage(), e);
                        errors.add(buildRowError(processDto.getExcelRowNumber(), "ROW", processDto.getProcessCode(),
                                "Error: " + e.getMessage()));
                    }
                }
                ProductLineResponse productLineResponse = productLineMapper.toDto(productLine);
                productLineResponse.setProcesses(processResponses);
                responses.add(productLineResponse);
            } catch (Exception e) {
                log.error("Error processing ProductLine group: {}", e.getMessage(), e);
                errors.add(buildSystemError("Error processing ProductLine: " + e.getMessage()));
            }
        }

        return responses;
    }

    /**
     * Find existing ProductLine by code or create new one
     */
    private ProductLine findOrCreateProductLine(String code, String name, List<ImportErrorItem> errors) {
        ProductLine productLine = productLineRepository.findByCode(code).orElseGet(ProductLine::new);
        productLine.setName(name);
        productLine.setCode(code);
        return productLineRepository.save(productLine);
    }

    /**
     * Find existing Process by code within ProductLine or create new one
     */
    private Process findOrCreateProcess(ProductLine productLine, ProductLineImportDto dto,
                                        List<ImportErrorItem> errors) {
        try {
            // Find existing process by ProductLine + Code
            return processRepository.findByProductLineCodeAndCode(productLine.getCode(), dto.getProcessCode())
                    .map(existing -> {
                        // Update existing
                        existing.setName(dto.getProcessName());
                        existing.setDescription(dto.getProcessDescription());
                        if (dto.getProcessClassification() != null
                                && !dto.getProcessClassification().trim().isEmpty()) {
                            try {
                                existing.setClassification(
                                        ProcessClassification.valueOf(dto.getProcessClassification()));
                            } catch (IllegalArgumentException e) {
                                log.warn("Invalid classification {}, keeping existing", dto.getProcessClassification());
                            }
                        }
                        log.info("Updated Process: code={}, name={}", dto.getProcessCode(), dto.getProcessName());
                        return processRepository.save(existing);
                    })
                    .orElseGet(() -> {
                        // Create new
                        Process newProcess = Process.builder()
                                .productLine(productLine)
                                .code(dto.getProcessCode())
                                .name(dto.getProcessName())
                                .description(dto.getProcessDescription())
                                .classification(getProcessClassification(dto.getProcessClassification()))
                                .build();
                        log.info("Created new Process: code={}, name={}", dto.getProcessCode(), dto.getProcessName());
                        return processRepository.save(newProcess);
                    });

        } catch (Exception e) {
            log.error("Error finding/creating Process with code {}: {}", dto.getProcessCode(), e.getMessage());
            errors.add(buildRowError(dto.getExcelRowNumber(), "processCode", dto.getProcessCode(),
                    "Error: " + e.getMessage()));
            return null;
        }
    }

    private ProcessClassification getProcessClassification(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ProcessClassification.C4; // default
        }

        try {
            int classificationInt = Integer.parseInt(value.trim());

            return switch (classificationInt) {
                case 1 -> ProcessClassification.C1;
                case 2 -> ProcessClassification.C2;
                case 3 -> ProcessClassification.C3;
                default -> ProcessClassification.C4;
            };

        } catch (NumberFormatException e) {
            log.warn("Invalid classification '{}', using default C4", value);
            return ProcessClassification.C4;
        }
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
                    ImportType.PRODUCT_LINE_IMPORT,
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
                    ImportType.PRODUCT_LINE_IMPORT,
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

    /**
     * Build row error item
     */
    private ImportErrorItem buildRowError(Integer rowNumber, String field, String value, String message) {
        return ImportErrorItem.builder()
                .rowNumber(rowNumber)
                .field(field)
                .value(value)
                .message(message)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductLineDetailResponse getProductLineFullDetail(Long lineId) {
        ProductLine line = productLineRepository.findById(lineId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_LINE_NOT_FOUND));

        ProductLineDetailResponse.ProductLineDetailResponseBuilder builder = ProductLineDetailResponse.builder()
                .lineId(line.getId())
                .lineCode(line.getCode())
                .lineName(line.getName());

        // Group → SV
        Group group = line.getGroup();
        if (group != null) {
            builder.groupId(group.getId())
                    .groupName(group.getName());
            if (group.getSupervisor() != null) {
                builder.supervisorId(group.getSupervisor().getId())
                        .supervisorName(group.getSupervisor().getFullName());
            }
            // Section → MNG
            Section section = group.getSection();
            if (section != null) {
                builder.sectionId(section.getId())
                        .sectionName(section.getName());
                if (section.getManager() != null) {
                    builder.managerId(section.getManager().getId())
                            .managerName(section.getManager().getFullName());
                }
            }
        }

        // Processes of this line
        List<Process> processes = processRepository.findByProductLineIdAndDeleteFlagFalse(lineId);
        List<ProductLineDetailResponse.ProcessInfo> processInfos = processes.stream()
                .map(p -> ProductLineDetailResponse.ProcessInfo.builder()
                        .processId(p.getId())
                        .processCode(p.getCode())
                        .processName(p.getName())
                        .classification(p.getClassification() != null ? p.getClassification().getValue() : null)
                        .standardTimeJt(p.getStandardTimeJt())
                        .build())
                .toList();
        builder.processes(processInfos);

        // Products linked to these processes (via product_process)
        List<Long> processIds = processes.stream().map(Process::getId).toList();
        Map<Long, List<ProductProcess>> ppByProduct = new java.util.HashMap<>();
        if (!processIds.isEmpty()) {
            for (Long pid : processIds) {
                List<ProductProcess> pps = productProcessRepository.findByProcessId(pid);
                for (ProductProcess pp : pps) {
                    ppByProduct.computeIfAbsent(pp.getProduct().getId(), k -> new ArrayList<>()).add(pp);
                }
            }
        }

        List<ProductLineDetailResponse.ProductWithProcesses> productList = ppByProduct.entrySet().stream()
                .map(entry -> {
                    Product product = entry.getValue().get(0).getProduct();
                    List<ProductLineDetailResponse.ProductProcessInfo> ppInfos = entry.getValue().stream()
                            .map(pp -> ProductLineDetailResponse.ProductProcessInfo.builder()
                                    .processId(pp.getProcess().getId())
                                    .processCode(pp.getProcess().getCode())
                                    .processName(pp.getProcess().getName())
                                    .standardTimeJt(pp.getStandardTimeJt())
                                    .build())
                            .toList();
                    return ProductLineDetailResponse.ProductWithProcesses.builder()
                            .productId(product.getId())
                            .productCode(product.getCode())
                            .productName(product.getName())
                            .description(product.getDescription())
                            .attachmentUrls(
                                    attachmentService.getAttachmentsByEntity("PRODUCT", product.getId())
                                            .stream().map(Attachment::getUrl).toList())
                            .processes(ppInfos)
                            .build();
                })
                .toList();
        builder.products(productList);

        return builder.build();
    }
}
