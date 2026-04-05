package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.DefectImportDto;
import com.sep490.anomaly_training_backend.dto.request.DefectProposalDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.DefectProposalRequest;
import com.sep490.anomaly_training_backend.dto.request.ImageData;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import com.sep490.anomaly_training_backend.dto.response.ProductResponse;
import com.sep490.anomaly_training_backend.dto.response.defect.*;
import com.sep490.anomaly_training_backend.enums.DefectType;
import com.sep490.anomaly_training_backend.enums.ImportStatus;
import com.sep490.anomaly_training_backend.enums.ImportType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.DefectMapper;
import com.sep490.anomaly_training_backend.mapper.DefectProposalDetailMapper;
import com.sep490.anomaly_training_backend.mapper.DefectProposalMapper;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.DefectService;
import com.sep490.anomaly_training_backend.service.ImportHistoryService;
import com.sep490.anomaly_training_backend.service.ProductService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import com.sep490.anomaly_training_backend.service.minio.ImportImageHandlerService;
import com.sep490.anomaly_training_backend.util.DefectCodeGenerator;
import com.sep490.anomaly_training_backend.util.helper.DefectImportHelper;
import com.sep490.anomaly_training_backend.util.validator.DefectImportValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefectServiceImpl implements DefectService {

    private final DefectRepository defectRepository;
    private final DefectMapper defectMapper;
    private final ProcessRepository processRepository;
    private final ProductRepository productRepository;
    private final ProductLineRepository productLineRepository;
    private final ImportHistoryService importHistoryService;
    private final DefectImportValidator importValidator;
    private final DefectImportHelper importHelper;
    private final ImportImageHandlerService importImageHandlerService;
    private final AttachmentService attachmentService;
    private final DefectCodeGenerator defectCodeGenerator;
    private final ProductService productService;
    private final TrainingSampleRepository trainingSampleRepository;
    private final DefectProposalRepository defectProposalRepository;
    private final DefectProposalMapper defectProposalMapper;
    private final UserRepository userRepository;
    private final DefectProposalDetailRepository defectProposalDetailRepository;
    private final ApprovalService approvalService;
    private final DefectProposalDetailMapper defectProposalDetailMapper;
    private final DefectProposalHistoryRepository defectProposalHistoryRepository;


//    @Override
//    public List<DefectResponse> getDefectBySupervisor(Long supervisorId) {
//        return defectRepository.findAllBySupervisorAndDeleteFlagFalseOrderByCreatedAtDesc(supervisorId)
//                .stream()
//                .map(defectMapper::toDto).toList();
//    }

    @Override
    public List<DefectResponse> getDefectByProductLine(Long productLineId) {
        List<Defect> defects = defectRepository.findAllByProductLineAndDeleteFlagFalseOrderByCreatedAtDesc(productLineId);
        return defects.stream().map(defect -> {
            DefectResponse response = defectMapper.toDto(defect);
            if (defect.getProduct() != null) {
                ProductResponse productResponse = productService.getProductById(defect.getProduct().getId());
                response.setProduct(productResponse);
            }
            addAttachment(response);
            return response;
        }).toList();
    }

    @Override
    public List<DefectResponse> getDefectByProcess(Long processId) {
        return defectRepository.findByProcessIdAndDeleteFlagFalse(processId)
                .stream()
                .map(defect -> {
                    DefectResponse response = defectMapper.toDto(defect);
                    if (defect.getProduct() != null) {
                        ProductResponse productResponse = productService.getProductById(defect.getProduct().getId());
                        response.setProduct(productResponse);
                    }
                    addAttachment(response);
                    return response;
                }).toList();
    }

    @Override
    public DefectResponse getDefectById(Long id) {
        Defect defect = defectRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.DEFECT_NOT_FOUND));
        DefectResponse response = defectMapper.toDto(defect);
        if (defect.getProduct() != null) {
            ProductResponse productResponse = productService.getProductById(defect.getProduct().getId());
            response.setProduct(productResponse);
        }
        return addAttachment(defectMapper.toDto(defect));
    }

    @Override
    public Boolean checkExistDefectDescription(String defectDescription) {
        return defectRepository.existsActiveByDefectDescriptionIgnoreCase(defectDescription);
    }

    @Override
    public List<DefectResponse> importDefect(User currentUser, MultipartFile file) {
        validateImportFile(file);

        List<ImportErrorItem> errors = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = getFirstSheet(workbook);
            List<DefectImportDto> parsedRows = importHelper.parseExcelRows(sheet, errors);
            ProductLine productLine = getProductLineFromHeader(sheet);
            if (productLine == null) {
                errors.add(buildSystemError("ProductLine not found in file header (Row 1)"));
                throw new AppException(ErrorCode.PRODUCT_LINE_NOT_IN_HEADER);
            }
            // Step 4: Validate only file data (NO DB validation)
            importValidator.validateFileData(parsedRows, errors);

            if (!errors.isEmpty()) {
                throw new AppException(ErrorCode.IMPORT_VALIDATION_ERROR);
            }

            // Step 5: Insert all rows (import only creates new defects)
            List<DefectResponse> responses = insertAllRows(parsedRows, productLine, currentUser, errors);

            // Step 6: Save success history
            saveImportPassHistory(currentUser, file);

            return responses;

        } catch (AppException e) {
            saveImportFailHistory(currentUser, file, errors);
            throw e;
        } catch (Exception e) {
            log.error("Import defect failed", e);
            if (errors.isEmpty()) {
                errors.add(buildSystemError("System error: " + e.getMessage()));
                saveImportFailHistory(currentUser, file, errors);
            }
            throw new AppException(ErrorCode.CANNOT_READ_EXCEL_FILE);
        }
    }

    @Override
    public DefectCoverageResponse getCoverageInProductLine(Long productLineId) {
        List<Defect> defects = defectRepository.findDefectsWithoutTrainingSampleOrderByCreatedAtDesc(productLineId);
        int totalDefect = defectRepository.findAllByProductLineAndDeleteFlagFalseOrderByCreatedAtDesc(productLineId).size();
        int totalTrainingSample = trainingSampleRepository.findByProductLineIdAndDeleteFlagFalseOrderByCreatedAtDesc(productLineId).size();
        List<Defect> totalDefectInProductLIne = defectRepository.findAllByProductLineAndDeleteFlagFalseOrderByCreatedAtDesc(productLineId);
        Double coverage = totalDefectInProductLIne.isEmpty() ? 0.0 : (double) (totalDefectInProductLIne.size() - defects.size()) / totalDefectInProductLIne.size() * 100;
        return DefectCoverageResponse.builder()
                .defects(defects.stream().map(defectMapper::toDto).toList())
                .coverageRate(coverage)
                .totalDefect((long) totalDefect)
                .totalTrainingSample((long) totalTrainingSample)
                .build();
    }

    @Override
    public List<DefectInProcess> countDefectInProcess(Long productLineId) {
        List<Process> processes = processRepository.findByProductLineIdAndDeleteFlagFalse(productLineId);
        List<DefectInProcess> defectsInProcess = new ArrayList<>();
        for (Process process : processes) {
            List<Defect> defects = defectRepository.findByProcessIdAndDeleteFlagFalse(process.getId());
            long totalDefect = defects.size();
            long totalDefectiveGood = 0;
            long totalClaim = 0;
            long totalStartedClaim = 0;
            for (Defect defect : defects) {
                if (defect.getDefectType().equals(DefectType.DEFECTIVE_GOODS)) {
                    totalDefectiveGood++;
                } else if (defect.getDefectType().equals(DefectType.STARTLED_CLAIM)) {
                    totalStartedClaim++;
                } else {
                    totalClaim++;
                }
            }
            DefectInProcess defectInProcess = DefectInProcess.builder()
                    .processId(process.getId())
                    .processCode(process.getCode())
                    .processName(process.getName())
                    .totalDefects(totalDefect)
                    .totalDefectiveGood(totalDefectiveGood)
                    .totalClaim(totalClaim)
                    .totalStartledClaim(totalStartedClaim)
                    .build();
            defectsInProcess.add(defectInProcess);
        }
        return defectsInProcess;
    }

    private void validateImportFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_IS_EMPTY);
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls"))) {
            throw new AppException(ErrorCode.INVALID_FILE_FORMAT);
        }
    }

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
     * Get ProductLine from Excel header row (Row 1)
     */
    private ProductLine getProductLineFromHeader(Sheet sheet) {
        try {
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return null;
            }

            Cell cell = headerRow.getCell(1);
            if (cell == null) {
                return null;
            }

            String productLineCode = getCellStringValue(cell);
            if (productLineCode == null || productLineCode.trim().isEmpty()) {
                return null;
            }

            return productLineRepository.findByCode(productLineCode.trim()).orElse(null);
        } catch (Exception e) {
            log.error("Error reading ProductLine from header", e);
            return null;
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                double value = cell.getNumericCellValue();
                if (value == (long) value) {
                    return String.valueOf((long) value);
                }
                return String.valueOf(value);
            default:
                return cell.toString();
        }
    }


    /**
     * Insert all parsed rows into database as new defects
     * - Always create new defects with auto-generated defect codes
     * - Handle images from Excel rows
     */
    private List<DefectResponse> insertAllRows(
            List<DefectImportDto> parsedRows,
            ProductLine productLine,
            User user, List<ImportErrorItem> errors) {

        List<DefectResponse> responses = new ArrayList<>();

        for (DefectImportDto dto : parsedRows) {
            Defect defect = insertNewDefect(dto, productLine, errors);
            handleDefectImages(dto.getImageData(), defect, user);
            responses.add(defectMapper.toDto(defect));
        }

        return responses;
    }

    private Defect insertNewDefect(DefectImportDto dto, ProductLine productLine, List<ImportErrorItem> errors) {
        Defect defect = new Defect();

        // Apply all fields
        applyImportDtoToDefect(dto, productLine, defect, errors);
        Defect saved = defectRepository.save(defect);

        log.info("Inserted new Defect: code={}, id={}",
                saved.getDefectCode(), saved.getId());
        return saved;
    }

    private void applyImportDtoToDefect(DefectImportDto dto, ProductLine productLine, Defect defect, List<ImportErrorItem> errors) {
        // Resolve and validate Process
        Process process = null;
        if (dto.getProcessCode() != null && !dto.getProcessCode().trim().isEmpty()) {
            process = processRepository.findByProductLineCodeAndCode(productLine.getCode(), dto.getProcessCode())
                    .orElseThrow(() -> addErrorAndReturn(
                            errors,
                            dto.getExcelRowNumber(),
                            "processCode",
                            dto.getProcessCode(),
                            "Process not found with code: " + dto.getProcessCode(),
                            ErrorCode.PROCESS_NOT_FOUND
                    ));
        }

        // Resolve and validate Product
        Product product = null;
        if (dto.getProductCode() != null && !dto.getProductCode().trim().isEmpty()) {
            product = productRepository.findByCode(dto.getProductCode())
                    .orElseThrow(() -> addErrorAndReturn(
                            errors,
                            dto.getExcelRowNumber(),
                            "productCode",
                            dto.getProductCode(),
                            "Product not found with code: " + dto.getProductCode(),
                            ErrorCode.PRODUCT_NOT_FOUND
                    ));
        }

        // Always generate new defect code for import
        defect.setDefectCode(defectCodeGenerator.generateDefectCode());

        defect.setDefectDescription(dto.getDefectDescription());
        defect.setDetectedDate(dto.getDetectedDate());
        defect.setNote(dto.getNote());
        defect.setOriginCause(dto.getOriginCause());
        defect.setOutflowCause(dto.getOutflowCause());
        defect.setCausePoint(dto.getCausePoint());
        defect.setOriginMeasures(dto.getOriginMeasures());
        defect.setOutflowMeasures(dto.getOutflowMeasures());
        if (dto.getIsEscape()) {
            defect.setDefectType(DefectType.DEFECTIVE_GOODS);
        } else if (dto.getStartledClaim()) {
            defect.setDefectType(DefectType.STARTLED_CLAIM);
        } else {
            defect.setDefectType(DefectType.CLAIM);
        }
        // Set 4 new fields from import
        defect.setCustomer(dto.getCustomer());
        defect.setQuantity(dto.getQuantity());
        defect.setConclusion(dto.getConclusion());

        // Set references
        defect.setProcess(process);
        defect.setProduct(product);
    }

    /**
     * Handle images for Defect from Excel row
     * - Extract images từ row
     * - Upload new images via ImportImageHandlerService
     */
    private void handleDefectImages(ImageData imageData, Defect defect, User user) {
        try {
            if (defect == null || defect.getId() == null) {
                log.debug("Defect has no ID, skipping image handling");
                return;
            }

            log.info("Handling images for Defect id={}", defect.getId());

            // Note: Image handling service is injected and will be called here
            // Currently images are not extracted from Excel in this import
            // Future: If Excel format includes images, call importImageHandlerService here
            importImageHandlerService.handleRowImages(imageData, "DEFECT", defect.getId(), user.getUsername());

        } catch (Exception e) {
            if (defect != null && defect.getId() != null) {
                log.error("Error handling images for Defect id={}: {}", defect.getId(), e.getMessage());
            } else {
                log.error("Error handling images for Defect: {}", e.getMessage());
            }
            // Don't throw - ảnh handling fail không nên block main import
        }
    }

    // ...existing code...

    private void saveImportFailHistory(User currentUser, MultipartFile file, List<ImportErrorItem> errors) {
        importHistoryService.saveHistory(
                currentUser,
                file.getOriginalFilename(),
                ImportType.DEFECT_IMPORT,
                ImportStatus.FAIL,
                errors
        );
    }

    private ImportErrorItem buildSystemError(String message) {
        return ImportErrorItem.builder()
                .field("SYSTEM")
                .message(message)
                .build();
    }

    private void saveImportPassHistory(User currentUser, MultipartFile file) {
        importHistoryService.saveHistory(
                currentUser,
                file.getOriginalFilename(),
                ImportType.DEFECT_IMPORT,
                ImportStatus.PASS,
                List.of()
        );
    }

    private AppException addErrorAndReturn(
            List<ImportErrorItem> errors,
            Integer rowNumber,
            String field,
            String value,
            String message,
            ErrorCode errorCode) {

        errors.add(ImportErrorItem.builder()
                .rowNumber(rowNumber)
                .field(field)
                .value(value)
                .message(message)
                .build());

        return new AppException(errorCode);
    }

    private DefectResponse addAttachment(DefectResponse defectResponse) {
        if (Objects.isNull(defectResponse)) {
            return null;
        }
        List<Attachment> attachments = attachmentService.getAttachmentsByEntity("DEFECT", defectResponse.getDefectId());
        List<String> imageUrls = attachments.stream()
                .map(Attachment::getUrl)
                .toList();
        defectResponse.setAttachmentUrls(imageUrls);
        return defectResponse;
    }

    @Override
    public List<DefectProposalResponse> getDefectProposalByProductLine(Long id, String username) {
        List<DefectProposalResponse> result = new ArrayList<>();
        List<DefectProposal> listEntity = new ArrayList<>();
        Role userRole = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND))
                .getRoles().stream().findFirst().orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        if (("ROLE_TEAM_LEADER").equals(userRole.getRoleCode())) {
            listEntity = defectProposalRepository.findByProductLineIdAndCreatedByOrderByCreatedAtDesc(id, username);
        } else {
            listEntity = defectProposalRepository.findByProductLineForSupervisorAndManagerOrderByCreatedAtDesc(id);
        }
        for (DefectProposal entity : listEntity) {
            result.add(defectProposalMapper.toResponse(entity, userRepository));
        }
        return result;
    }

    @Override
    @Transactional
    public DefectProposalResponse createDefectProposalDraft(DefectProposalRequest reportRequest, User currentUser) {
        return defectProposalMapper.toResponse(createProposal(reportRequest, currentUser), userRepository);
    }

    @Override
    public void deleteDefectProposal(Long id) {
        DefectProposal entity = defectProposalRepository.findById(id).orElse(null);
        if (entity != null) {
            entity.setDeleteFlag(true);
            defectProposalRepository.save(entity);
        }
    }

    @Override
    public DefectProposalUpdateResponse updateDefectProposal(Long id, DefectProposalRequest request, User currentUser) {
        DefectProposal proposal = defectProposalRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEFECT_PROPOSAL_NOT_FOUND));

        List<DefectProposalDetailRequest> items = request.getListDetail();
        if (items == null || items.isEmpty()) {
            throw new AppException(ErrorCode.PROPOSAL_HAS_NO_DETAILS);
        }
        // load existing details (of this proposal)
        List<DefectProposalDetail> existingDetails = defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(id);
        Map<Long, DefectProposalDetail> existingMap = new HashMap<>();
        for (DefectProposalDetail detail : existingDetails) {
            existingMap.put(detail.getId(), detail);
        }
        // validate ids belong to proposal
        for (DefectProposalDetailRequest item : items) {
            Long detailId = item.getDefectProposalDetailId();
            if (detailId != null && !existingMap.containsKey(detailId)) {
                throw new AppException(ErrorCode.INVALID_DETAIL_ID_FOR_PROPOSAL);
            }
        }
        // dùng để xác định những detail nào còn tồn tại trong request
        Set<Long> requestDetailIds = new HashSet<>();
        //apply create/update/delete
        for (DefectProposalDetailRequest item : items) {
            // create
            if (item.getDefectProposalDetailId() == null) {
                DefectProposalDetail newEntity = mapToEntity(item, proposal, currentUser);
                newEntity = defectProposalDetailRepository.save(newEntity);
                if (item.getImages() != null && !item.getImages().isEmpty()) {
                    attachmentService.uploadAttachments(item.getImages(), "DEFECT_PROPOSAL", newEntity.getId(), currentUser.getUsername());
                }
                continue;
            }
            requestDetailIds.add(item.getDefectProposalDetailId());
            DefectProposalDetail entity = existingMap.get(item.getDefectProposalDetailId());
            if (entity == null) {
                throw new AppException(ErrorCode.INVALID_DETAIL_ID_FOR_PROPOSAL);
            }
            if (item.getDefectId() != null) {
                Defect defectRef = defectRepository.getReferenceById(item.getDefectId());
                entity.setDefect(defectRef);
            } else {
                entity.setDefect(null);
            }

            // Validate and set product - can be null
            if (item.getProductId() != null) {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
                entity.setProduct(product);
            } else {
                entity.setProduct(null);
            }

            if (item.getProcessId() == null) {
                throw new AppException(ErrorCode.MISSING_PROCESS_ID);
            }
            Process processRef = processRepository.getReferenceById(item.getProcessId());
            entity.setProcess(processRef);
            entity.setProposalType(item.getProposalType());
            entity.setDefectDescription(item.getDefectDescription());
            entity.setDetectedDate(item.getDetectedDate());
            entity.setNote(item.getNote());
            entity.setOriginCause(item.getOriginCause());
            entity.setOutflowCause(item.getOutflowCause());
            entity.setCausePoint(item.getCausePoint());
            entity.setOriginMeasures(item.getOriginMeasures());
            entity.setOutflowMeasures(item.getOutflowMeasures());
            entity.setDefectType(DefectType.valueOf(item.getDefectType()));

            // Set new fields
            entity.setCustomer(item.getCustomer());
            entity.setQuantity(item.getQuantity());
            entity.setConclusion(item.getConclusion());
            if (item.getImages() != null && !item.getImages().isEmpty()) {
                attachmentService.deleteAttachments("DEFECT_PROPOSAL", item.getDefectProposalDetailId());
                attachmentService.uploadAttachments(item.getImages(), "DEFECT_PROPOSAL", entity.getId(), currentUser.getUsername());
            }
            defectProposalDetailRepository.save(entity);
        }
        for (DefectProposalDetail existing : existingDetails) {
            if (!requestDetailIds.contains(existing.getId())) {
                existing.setDeleteFlag(true);
                defectProposalDetailRepository.save(existing);
            }
        }
        DefectProposal updatedProposal = defectProposalRepository.save(proposal);
        List<DefectProposalDetail> latestDetails = defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(updatedProposal.getId());
        DefectProposalUpdateResponse response = new DefectProposalUpdateResponse();
        response.setId(updatedProposal.getId());
        response.setProductLineId(proposal.getProductLine() != null ? proposal.getProductLine().getId() : null);

        List<DefectProposalDetailUpdateResponse> detailResponses = new ArrayList<>();
        for (DefectProposalDetail detail : latestDetails) {
            detailResponses.add(mapToResponse(detail));
        }
        response.setDefectProposalDetail(detailResponses);
        return response;
    }

    @Override
    public void submitDefectProposalForApproval(Long proposalId, User currentUser, HttpServletRequest request) {
        DefectProposal proposal = defectProposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.DEFECT_PROPOSAL_NOT_FOUND));
        if (!proposal.getCreatedBy().equals(currentUser.getUsername())) {
            throw new AppException(ErrorCode.ONLY_AUTHOR_CAN_EDIT);
        }
        validateProposalForSubmission(proposal);
        approvalService.submit(proposal, currentUser, request);
        defectProposalRepository.save(proposal);
    }

    @Override
    public void approve(Long proposalId, User currentUser, ApproveRequest req, HttpServletRequest request) {
        DefectProposal proposal = defectProposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.DEFECT_PROPOSAL_NOT_FOUND));
        approvalService.approve(proposal, currentUser, req, request);
        defectProposalRepository.save(proposal);
    }

    @Override
    public void reject(Long proposalId, User currentUser, RejectRequest req, HttpServletRequest request) {
        DefectProposal proposal = defectProposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.DEFECT_PROPOSAL_NOT_FOUND));
        approvalService.reject(proposal, currentUser, req, request);
        defectProposalRepository.save(proposal);
    }

    @Override
    public ResponseEntity<Boolean> canApprove(Long proposalId, User currentUser) {
        try {
            DefectProposal proposal = defectProposalRepository.findById(proposalId)
                    .orElseThrow(() -> new AppException(ErrorCode.DEFECT_PROPOSAL_NOT_FOUND));
            Boolean hasPermission = approvalService.canApprove(proposal, currentUser);
            return ResponseEntity.ok(hasPermission);
        } catch (AppException e) {

            return ResponseEntity.ok(Boolean.FALSE);
        }
    }

    @Override
    public void revise(Long id, User currentUser, HttpServletRequest request) {
        DefectProposal proposal = defectProposalRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEFECT_PROPOSAL_NOT_FOUND));
        if (!currentUser.getUsername().equals(proposal.getCreatedBy())) {
            throw new AppException(ErrorCode.ONLY_AUTHOR_CAN_EDIT);
        }
        createHistorySnapshot(proposal);
        approvalService.revise(proposal, currentUser, request);
    }

    private void createHistorySnapshot(DefectProposal proposal) {
        DefectProposalHistory history = DefectProposalHistory.builder()
                .defectProposal(proposal)
                .version(proposal.getCurrentVersion() == null ? 1 : proposal.getCurrentVersion())
                .formCode(proposal.getFormCode())
                .recordedAt(LocalDateTime.now())
                .productLineId(proposal.getProductLine().getId() != null ? proposal.getProductLine().getId() : null)
                .detailHistory(new ArrayList<>())
                .build();

        if (proposal.getDetails() != null) {
            for (DefectProposalDetail detail : proposal.getDetails()) {
                DefectProposalDetailHistory detailHistory = DefectProposalDetailHistory.builder()
                        .defectProposalHistory(history)
                        .defectId(detail.getDefect() != null ? detail.getDefect().getId() : null)
                        .proposalType(detail.getProposalType() != null ? detail.getProposalType().toString() : null)
                        .defectDescription(detail.getDefectDescription())
                        .processId(detail.getProcess() != null ? detail.getProcess().getId() : null)
                        .processCode(detail.getProcess() != null ? detail.getProcess().getCode() : null)
                        .processName(detail.getProcess() != null ? detail.getProcess().getName() : null)
                        .detectedDate(detail.getDetectedDate())
                        .note(detail.getNote())
                        .originCause(detail.getOriginCause())
                        .outflowCause(detail.getOutflowCause())
                        .causePoint(detail.getCausePoint())
                        .originMeasures(detail.getOriginMeasures())
                        .outflowMeasures(detail.getOutflowMeasures())
                        .defectType(detail.getDefectType())
                        .build();
                history.getDetailHistory().add(detailHistory);
            }
        }

        defectProposalHistoryRepository.save(history);
    }


    private void createDetail(List<DefectProposalDetailRequest> DefectProposalDetailList, DefectProposal proposal, User currentUser) {
        for (DefectProposalDetailRequest detailRequest : DefectProposalDetailList) {
            Process process = processRepository.findById(detailRequest.getProcessId())
                    .orElseThrow(() -> new AppException(ErrorCode.PROCESS_NOT_FOUND));
            DefectProposalDetail entity = new DefectProposalDetail();
            entity.setDefectProposal(proposal);

            // Handle defect - can be null
            if (detailRequest.getDefectId() != null) {
                Defect defect = defectRepository.findById(detailRequest.getDefectId())
                        .orElseThrow(() -> new AppException(ErrorCode.DEFECT_NOT_FOUND));
                entity.setDefect(defect);
            }

            // Handle product - can be null
            if (detailRequest.getProductId() != null) {
                Product product = productRepository.findById(detailRequest.getProductId())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
                entity.setProduct(product);
            }

            entity.setProposalType(detailRequest.getProposalType());
            entity.setDefectDescription(detailRequest.getDefectDescription());
            entity.setProcess(process);
            entity.setDetectedDate(detailRequest.getDetectedDate());
            entity.setNote(detailRequest.getNote());
            entity.setOriginCause(detailRequest.getOriginCause());
            entity.setOutflowCause(detailRequest.getOutflowCause());
            entity.setCausePoint(detailRequest.getCausePoint());
            entity.setOriginMeasures(detailRequest.getOriginMeasures());
            entity.setOutflowMeasures(detailRequest.getOutflowMeasures());
            entity.setDefectType(DefectType.valueOf(detailRequest.getDefectType()));

            // Set new fields
            entity.setCustomer(detailRequest.getCustomer());
            entity.setQuantity(detailRequest.getQuantity());
            entity.setConclusion(detailRequest.getConclusion());

            // Save detail first to get ID for attachment
            entity = defectProposalDetailRepository.save(entity);

            // Upload images for this detail if provided
            if (detailRequest.getImages() != null && !detailRequest.getImages().isEmpty()) {
                attachmentService.uploadAttachments(detailRequest.getImages(), "DEFECT_PROPOSAL", entity.getId(), currentUser.getUsername());
            }
        }
    }

    private DefectProposalDetail mapToEntity(DefectProposalDetailRequest request, DefectProposal proposal, User user) {
        if (request == null) throw new AppException(ErrorCode.INVALID_REQUEST_FORMAT);
        if (request.getProcessId() == null) throw new AppException(ErrorCode.MISSING_PROCESS_ID);
        if (request.getProposalType() == null) throw new AppException(ErrorCode.MISSING_PROPOSAL_TYPE);
        if (request.getDefectDescription() == null || request.getDefectDescription().isBlank()) {
            throw new AppException(ErrorCode.MISSING_DEFECT_DESCRIPTION);
        }
        if (request.getProductId() == null) throw new AppException(ErrorCode.MISSING_PRODUCT_ID);
        if (request.getDetectedDate() == null) throw new AppException(ErrorCode.MISSING_DETECTED_DATE);

        DefectProposalDetail entity = new DefectProposalDetail();
        entity.setDefectProposal(proposal);

        if (request.getDefectId() != null) {
            Defect defect = defectRepository.getReferenceById(request.getDefectId());
            entity.setDefect(defect);
        } else {
            entity.setDefect(null);
        }

        // Validate and set product - can be null
        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            entity.setProduct(product);
        }

        Process process = processRepository.getReferenceById(request.getProcessId());
        entity.setProcess(process);
        entity.setProposalType(request.getProposalType());
        entity.setDefectDescription(request.getDefectDescription());
        entity.setDetectedDate(request.getDetectedDate());

        entity.setNote(request.getNote());
        entity.setOriginCause(request.getOriginCause());
        entity.setOutflowCause(request.getOutflowCause());
        entity.setCausePoint(request.getCausePoint());

        entity.setOriginMeasures(request.getOriginMeasures());
        entity.setOutflowMeasures(request.getOutflowMeasures());
        entity.setDefectType(DefectType.valueOf(request.getDefectType()));

        // Set new fields
        entity.setCustomer(request.getCustomer());
        entity.setQuantity(request.getQuantity());
        entity.setConclusion(request.getConclusion());


        entity.setDeleteFlag(false);

        return entity;
    }

    private DefectProposalDetailUpdateResponse mapToResponse(DefectProposalDetail entity) {
        if (entity == null) return null;
        DefectProposalDetailUpdateResponse response = new DefectProposalDetailUpdateResponse();

        response.setId(entity.getId());

        if (entity.getDefect() != null) {
            response.setDefectId(entity.getDefect().getId());
        }

        response.setProposalType(entity.getProposalType());
        response.setDefectDescription(entity.getDefectDescription());

        if (entity.getProcess() != null) {
            response.setProcessId(entity.getProcess().getId());
            response.setProcessName(entity.getProcess().getName());
        }
        response.setDetectedDate(entity.getDetectedDate());
        response.setNote(entity.getNote());
        response.setOriginCause(entity.getOriginCause());
        response.setOutflowCause(entity.getOutflowCause());
        response.setCausePoint(entity.getCausePoint());
        response.setDeleteFlag(entity.isDeleteFlag());
        response.setOriginMeasures(entity.getOriginMeasures());
        response.setOutflowMeasures(entity.getOutflowMeasures());
        response.setDefectType(entity.getDefectType().toString());
        return response;
    }

    private void validateProposalForSubmission(DefectProposal proposal) {
        if (proposal.getDetails() == null || proposal.getDetails().isEmpty()) {
            throw new AppException(ErrorCode.PROPOSAL_HAS_NO_DETAILS);
        }
        for (DefectProposalDetail detail : proposal.getDetails()) {
            if (detail.getProcess() == null) {
                throw new AppException(ErrorCode.MISSING_PROCESS_IN_DETAIL);
            }
            if (detail.getProposalType() == null) {
                throw new AppException(ErrorCode.MISSING_PROPOSAL_TYPE);
            }
            if (detail.getDefectDescription() == null || detail.getDefectDescription().isBlank()) {
                throw new AppException(ErrorCode.MISSING_DEFECT_DESCRIPTION);
            }
            if (detail.getDetectedDate() == null) {
                throw new AppException(ErrorCode.MISSING_DETECTED_DATE);
            }
        }
    }

    private DefectProposal createProposal(DefectProposalRequest reportRequest, User currentUser) {
        ProductLine productLine = productLineRepository.findById(reportRequest.getProductLineId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_LINE_NOT_FOUND));
        DefectProposal proposalHeader = new DefectProposal();
        proposalHeader.setProductLine(productLine);
        proposalHeader.setStatus(ReportStatus.DRAFT);
        proposalHeader = defectProposalRepository.save(proposalHeader);
        createDetail(reportRequest.getListDetail(), proposalHeader, currentUser);
        return proposalHeader;
    }

    @Override
    public List<DefectProposalDetailResponse> getDefectProposalDetails(Long defectProposalId) {
        List<DefectProposalDetail> responsesList = defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(defectProposalId);
        return responsesList.stream().map(detail -> {
            DefectProposalDetailResponse responseItem = defectProposalDetailMapper.toResponse(detail);
            if (detail.getProduct() != null) {
                responseItem.setProduct(productService.getProductById(detail.getProduct().getId()));
            }
            if (detail.getRejectFeedback() != null) {
                responseItem.setRejectFeedback(detail.getRejectFeedback());
            }
            return addAttachment(responseItem);
        }).toList();
    }

    private DefectProposalDetailResponse addAttachment(DefectProposalDetailResponse response) {
        if (Objects.isNull(response)) {
            return null;
        }
        List<Attachment> attachments = attachmentService.getAttachmentsByEntity("DEFECT_PROPOSAL", response.getDefectProposalDetailId());
        List<String> imageUrls = attachments.stream()
                .map(Attachment::getUrl)
                .toList();
        response.setAttachmentUrls(imageUrls);
        return response;
    }
}
