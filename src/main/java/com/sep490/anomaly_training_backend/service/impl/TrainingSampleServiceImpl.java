package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.ImageData;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleImportDto;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleProposalDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleProposalRequest;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import com.sep490.anomaly_training_backend.dto.response.sample.*;
import com.sep490.anomaly_training_backend.enums.ImportStatus;
import com.sep490.anomaly_training_backend.enums.ImportType;
import com.sep490.anomaly_training_backend.enums.ProposalType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleMapper;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleProposalDetailMapper;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleProposalMapper;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.DefectService;
import com.sep490.anomaly_training_backend.service.ImportHistoryService;
import com.sep490.anomaly_training_backend.service.ProductService;
import com.sep490.anomaly_training_backend.service.TrainingSampleService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import com.sep490.anomaly_training_backend.service.minio.ImportImageHandlerService;
import com.sep490.anomaly_training_backend.util.TrainingCodeGenerator;
import com.sep490.anomaly_training_backend.util.helper.TrainingSampleImportHelper;
import com.sep490.anomaly_training_backend.util.validator.TrainingSampleImportValidator;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TrainingSampleServiceImpl implements TrainingSampleService {

    private final TrainingSampleRepository trainingSampleRepository;
    private final ProcessRepository processRepository;
    private final DefectRepository defectRepository;
    private final ProductRepository productRepository;
    private final ProductLineRepository productLineRepository;
    private final TrainingSampleMapper trainingSampleMapper;
    private final ImportHistoryService importHistoryService;
    private final TrainingSampleImportHelper importHelper;
    private final TrainingSampleImportValidator importValidator;
    private final ImportImageHandlerService importImageHandlerService;
    private final AttachmentService attachmentService;
    private final TrainingCodeGenerator trainingCodeGenerator;
    private final ProductService productService;
    private final DefectService defectService;
    private final TrainingSampleProposalRepository trainingSampleProposalRepository;
    private final TrainingSampleProposalDetailRepository trainingSampleProposalDetailRepository;
    private final TrainingSampleProposalMapper trainingSampleProposalMapper;
    private final UserRepository userRepository;
    private final ApprovalService approvalService;
    private final TrainingSampleProposalDetailMapper trainingSampleProposalDetailMapper;
    private final TrainingSampleProposalHistoryRepository trainingSampleProposalHistoryRepository;


    @Override
    public List<TrainingSampleResponse> getTrainingSampleByProductLine(Long productLineId) {
        return trainingSampleRepository.findByProductLineIdAndDeleteFlagFalseOrderByCreatedAtDesc(productLineId)
                .stream()
                .map(this::enrichResponse)
                .toList();
    }

    @Override
    public TrainingSampleResponse getTrainingSampleById(Long id) {
        TrainingSample entity = trainingSampleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_NOT_FOUND));
        return enrichResponse(entity);
    }

    @Override
    public List<TrainingSampleResponse> getTrainingSampleByProcess(Long id) {
        TrainingSample entity = trainingSampleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_NOT_FOUND));
        return trainingSampleRepository.findByProcessIdAndDeleteFlagFalseOrderByCreatedAtDesc(entity.getProcess().getId())
                .stream().map(this::enrichResponse).toList();
    }

    @Override
    public List<TrainingSampleResponse> getTrainingSampleByCategory(Long id) {
        TrainingSample entity = trainingSampleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_NOT_FOUND));
        return trainingSampleRepository.findByCategoryNameAndDeleteFlagFalseOrderByCreatedAtDesc(entity.getCategoryName())
                .stream().map(this::enrichResponse).toList();
    }

    @Override
    public List<TrainingSampleResponse> importTrainingSample(User currentUser, MultipartFile file) {
        List<ImportErrorItem> errors = new ArrayList<>();
        try {
            validateImportFile(file);
            try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
                Sheet sheet = getFirstSheet(workbook);
                ProductLine productLine = getProductLineFromHeader(sheet);
                if (productLine == null) {
                    errors.add(buildSystemError("ProductLine not found in file header (Row 1)"));
                    throw new AppException(ErrorCode.PRODUCT_LINE_NOT_IN_HEADER);
                }
                List<TrainingSampleImportDto> parsedRows = importHelper.parseExcelRowsWithCarryForward(sheet, errors);
                if (!errors.isEmpty()) {
                    throw new AppException(ErrorCode.IMPORT_PARSE_ERROR);
                }
                importValidator.validateFileData(parsedRows, errors);
                if (!errors.isEmpty()) {
                    throw new AppException(ErrorCode.IMPORT_VALIDATION_ERROR);
                }
                List<TrainingSampleResponse> responses = upsertAllRows(parsedRows, productLine, currentUser, errors);
                saveImportPassHistory(currentUser, file);
                return responses;
            } catch (AppException e) {
                if (!errors.isEmpty()) {
                    saveImportFailHistory(currentUser, file, errors);
                }
                throw e;
            } catch (Exception e) {
                log.error("Import training sample failed", e);
                if (errors.isEmpty()) {
                    errors.add(buildSystemError("System error: " + e.getMessage()));
                }
                saveImportFailHistory(currentUser, file, errors);
                throw new AppException(ErrorCode.CANNOT_READ_EXCEL_FILE);
            }

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during import", e);
            throw new AppException(ErrorCode.UNEXPECTED_IMPORT_ERROR);
        }
    }

    @Override
    public CategorySample getCategory(Long productLineId) {
        return CategorySample.builder()
                .categoryNames(trainingSampleRepository.getCategoryNames(productLineId))
                .trainingDescriptions(trainingSampleRepository.getTrainingDescriptions(productLineId))
                .build();
    }

    /**
     * Upsert all parsed rows into database
     * - If trainingCode is null: always INSERT new
     * - If trainingCode is not null: find existing, UPDATE if found OR CREATE if new
     * - Handle images from Excel rows
     * - Soft-delete old attachments when updating
     */
    private List<TrainingSampleResponse> upsertAllRows(
            List<TrainingSampleImportDto> parsedRows,
            ProductLine productLine, User user,
            List<ImportErrorItem> errors) {
        List<TrainingSampleResponse> responses = new ArrayList<>();
        for (TrainingSampleImportDto dto : parsedRows) {
            TrainingSample trainingSample = upsertTrainingSample(dto, productLine, errors);
            // If updating, soft-delete old attachments before handling new ones
            if (dto.getTrainingCode() != null) {
                attachmentService.deleteAttachments("TRAINING_SAMPLE", trainingSample.getId());
            }
            handleTrainingSampleImages(dto.getImageData(), trainingSample, user);
            responses.add(trainingSampleMapper.toDto(trainingSample));
        }
        return responses;
    }

    /**
     * Upsert one TrainingSample by trainingCode
     * - If trainingCode is null: always create new
     * - If trainingCode is not null: find by trainingCode, update if exists or create if new
     * - Validate all reference fields (process, defect, product)
     * - Soft-delete old attachments when updating
     */
    private TrainingSample upsertTrainingSample(
            TrainingSampleImportDto dto,
            ProductLine productLine, List<ImportErrorItem> errors) {

        // Find existing by trainingCode only if trainingCode is not null
        TrainingSample sample;
        if (dto.getTrainingCode() != null && !dto.getTrainingCode().trim().isEmpty()) {
            sample = trainingSampleRepository
                    .findByTrainingCode(dto.getTrainingCode())
                    .orElseThrow(() -> addErrorAndReturn(
                            errors,
                            dto.getExcelRowNumber(),
                            "trainingCode",
                            dto.getProcessCode(),
                            "Training sample not found with code : " + dto.getTrainingCode(),
                            ErrorCode.TRAINING_SAMPLE_NOT_FOUND));
        } else {
            // If trainingCode is null, always create new
            sample = new TrainingSample();
            sample.setTrainingCode(trainingCodeGenerator.generateTrainingCode());
        }

        // If updating, soft-delete old attachments before handling new ones
        if (sample.getId() != null) {
            attachmentService.deleteAttachments("TRAINING_SAMPLE", sample.getId());
        }

        // Resolve and validate Process
        Process process = null;
        if (dto.getProcessCode() != null && !dto.getProcessCode().trim().isEmpty()) {
            process = processRepository.findByProductLineCodeAndCode(productLine.getCode(), dto.getProcessCode())
                    .orElseThrow(() -> addErrorAndReturn(
                            errors,
                            dto.getExcelRowNumber(),
                            "processCode",
                            dto.getProcessCode(),
                            "Process not found in productline with code : " + dto.getProcessCode(),
                            ErrorCode.PROCESS_NOT_FOUND));
        }

        // Resolve and validate Defect
        Defect defect = null;
        if (dto.getDefectCode() != null && !dto.getDefectCode().trim().isEmpty()) {
            defect = defectRepository.findByDefectCode(dto.getDefectCode())
                    .orElseThrow(() -> addErrorAndReturn(
                            errors,
                            dto.getExcelRowNumber(),
                            "defectCode",
                            dto.getDefectCode(),
                            "Defect not found with code: " + dto.getDefectCode(),
                            ErrorCode.DEFECT_NOT_FOUND
                    ));
        }

        sample.setProcess(process);
        sample.setProductLine(productLine);
        sample.setCategoryName(dto.getCategoryName());
        sample.setTrainingDescription(dto.getTrainingDescription());
        sample.setTrainingSampleCode(dto.getTrainingSampleCode());
        sample.setDefect(defect);
        sample.setProducts(new HashSet<>()); // Products managed via proposal, not imported from Excel
        sample.setProcessOrder(dto.getProcessOrder());
        sample.setCategoryOrder(dto.getCategoryOrder());
        sample.setContentOrder(dto.getContentOrder());
        sample.setNote(dto.getNote());
        return trainingSampleRepository.save(sample);
    }

    /**
     * Handle images for TrainingSample from Excel row
     * - Extract images từ row
     * - Delete old attachments (if updating existing record)
     * - Upload new images
     */
    private void handleTrainingSampleImages(ImageData imageData, TrainingSample sample, User user) {
        try {
            if (sample.getId() == null) {
                log.debug("Sample has no ID, skipping image handling");
                return;
            }
            importImageHandlerService.handleRowImages(imageData, "TRAINING_SAMPLE", sample.getId(), user.getUsername());
        } catch (Exception e) {
            log.error("Error handling images for TrainingSample id={}: {}", sample.getId(), e.getMessage());
            // Don't throw - ảnh handling fail không nên block main import
        }
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

    // ============= Validation Methods =============

    private void validateImportFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_IS_EMPTY);
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null ||
                (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls"))) {
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
     * Get string value from cell
     */
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

    // ============= Import History Methods =============

    private void saveImportFailHistory(User currentUser, MultipartFile file, List<ImportErrorItem> errors) {
        importHistoryService.saveHistory(
                currentUser,
                file.getOriginalFilename(),
                ImportType.TRAINING_SAMPLE_IMPORT,
                ImportStatus.FAIL,
                errors
        );
    }

    private void saveImportPassHistory(User currentUser, MultipartFile file) {
        importHistoryService.saveHistory(
                currentUser,
                file.getOriginalFilename(),
                ImportType.TRAINING_SAMPLE_IMPORT,
                ImportStatus.PASS,
                List.of()
        );
    }

    private ImportErrorItem buildSystemError(String message) {
        return ImportErrorItem.builder()
                .rowNumber(null)
                .field("SYSTEM")
                .value(null)
                .message(message)
                .build();
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

    private TrainingSampleResponse addAttachment(TrainingSampleResponse response) {
        if (Objects.isNull(response)) {
            return null;
        }
        List<Attachment> attachments = attachmentService.getAttachmentsByEntity("TRAINING_SAMPLE", response.getTrainingSampleId());
        List<String> imageUrls = attachments.stream()
                .map(Attachment::getUrl)
                .toList();
        response.setAttachmentUrls(imageUrls);
        return response;
    }

    private TrainingSampleResponse enrichResponse(TrainingSample entity) {
        TrainingSampleResponse response = trainingSampleMapper.toDto(entity);
        if (entity.getProducts() != null && !entity.getProducts().isEmpty()) {
            response.setProducts(entity.getProducts().stream()
                    .map(p -> productService.getProductById(p.getId()))
                    .collect(Collectors.toList()));
        }
        if (entity.getDefect() != null) {
            response.setDefect(defectService.getDefectById(entity.getDefect().getId()));
        }
        return addAttachment(response);
    }

    @Override
    public List<TrainingSampleProposalDetailResponse> getTrainingSampleProposalDetails(Long trainingTopicReportId) {
        return trainingSampleProposalDetailRepository.findByTrainingSampleProposalIdAndDeleteFlagFalse(trainingTopicReportId)
                .stream()
                .map(this::enrichResponse)
                .toList();
    }

    private TrainingSampleProposalDetailResponse addAttachment(TrainingSampleProposalDetailResponse response) {
        if (Objects.isNull(response)) {
            return null;
        }
        List<Attachment> attachments = attachmentService.getAttachmentsByEntity("TRAINING_SAMPLE_PROPOSAL", response.getTrainingSampleProposalDetailId());
        List<String> imageUrls = attachments.stream()
                .map(Attachment::getUrl)
                .toList();
        response.setAttachmentUrls(imageUrls);
        return response;
    }

    private TrainingSampleProposalDetailResponse enrichResponse(TrainingSampleProposalDetail entity) {
        TrainingSampleProposalDetailResponse response = trainingSampleProposalDetailMapper.toResponse(entity);
        if (entity.getRejectFeedback() != null) {
            response.setRejectFeedback(response.getRejectFeedback());
        }
        if (entity.getProducts() != null && !entity.getProducts().isEmpty()) {
            response.setProducts(entity.getProducts().stream()
                    .map(p -> productService.getProductById(p.getId()))
                    .collect(Collectors.toList()));
        }
        if (entity.getDefect() != null) {
            response.setDefect(defectService.getDefectById(entity.getDefect().getId()));
        }
        return addAttachment(response);
    }

    @Override
    public List<TrainingSampleProposalResponse> getTrainingSampleProposalByProductLine(Long id, String username) {

        List<TrainingSampleProposal> entityList = new ArrayList<>();
        Role userRole = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND))
                .getRoles().stream().findFirst().orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        if (("ROLE_TEAM_LEADER").equals(userRole.getRoleCode())) {
            entityList = trainingSampleProposalRepository.findByProductLineIdAndCreatedByOrderByCreatedAtDesc(id, username);
        } else {
            entityList = trainingSampleProposalRepository.findByProductLineForSupervisorAndManagerOrderByCreatedAtDesc(id);
        }
        List<TrainingSampleProposalResponse> trainingSampleProposalResponses = new ArrayList<>();
        for (TrainingSampleProposal entity : entityList) {
            trainingSampleProposalResponses.add(trainingSampleProposalMapper.toResponse(entity, userRepository));
        }
        return trainingSampleProposalResponses;
    }

    @Override
    public TrainingSampleProposalResponse createTrainingSampleProposal(TrainingSampleProposalRequest proposalRequest, User currentUser) {
        ProductLine productLine = productLineRepository.findById(proposalRequest.getProductLineId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_LINE_NOT_FOUND));
        TrainingSampleProposal proposal = new TrainingSampleProposal();
        proposal.setProductLine(productLine);
        proposal.setStatus(ReportStatus.DRAFT);
        proposal = trainingSampleProposalRepository.save(proposal);
        createDetail(proposalRequest.getListDetail(), proposal, currentUser);
        return trainingSampleProposalMapper.toResponse(proposal, userRepository);
    }

    @Override
    public void deleteTrainingSampleProposal(Long id) {
        TrainingSampleProposal entity = trainingSampleProposalRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_PROPOSAL_NOT_FOUND));
        entity.setDeleteFlag(true);
        trainingSampleProposalRepository.save(entity);
    }

    @Override
    public TrainingSampleProposalUpdateResponse updateTrainingSampleProposal(Long id, TrainingSampleProposalRequest request, User currentUser) {
        List<TrainingSampleProposalDetailRequest> items = request.getListDetail();
        if (items == null || items.isEmpty()) {
            throw new AppException(ErrorCode.PROPOSAL_HAS_NO_DETAILS);
        }

        TrainingSampleProposal proposal = trainingSampleProposalRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_PROPOSAL_NOT_FOUND));

        if (request.getProductLineId() != null) {
            ProductLine productLine = productLineRepository.getReferenceById(request.getProductLineId());
            proposal.setProductLine(productLine);
        }

        List<TrainingSampleProposalDetail> existingDetails =
                trainingSampleProposalDetailRepository.findByTrainingSampleProposalIdAndDeleteFlagFalse(id);

        Map<Long, TrainingSampleProposalDetail> existingMap = new HashMap<>();
        for (TrainingSampleProposalDetail detail : existingDetails) {
            existingMap.put(detail.getId(), detail);
        }

        Set<Long> requestDetailIds = new HashSet<>();
        //Validate detail belong to proposal
        for (TrainingSampleProposalDetailRequest item : items) {
            if (item.getTrainingSampleProposalDetailId() != null && !existingMap.containsKey(item.getTrainingSampleProposalDetailId())) {
                throw new AppException(ErrorCode.INVALID_DETAIL_ID_FOR_PROPOSAL);
            }
        }
        for (TrainingSampleProposalDetailRequest item : items) {
            if (item.getTrainingSampleProposalDetailId() == null) {
                TrainingSampleProposalDetail newEntity = mapToEntity(item, proposal);
                ProposalType inferredType = resolveProposalType(item);
                List<Long> pIds = item.getProductIds() != null ? item.getProductIds() : Collections.emptyList();
                TrainingSample validateEntity = pIds.isEmpty() ? null : trainingSampleRepository.checkExist(item.getProcessId(),
                                item.getCategoryName(),
                                item.getTrainingDescription(),
                                item.getTrainingSampleCode(),
                                pIds)
                        .orElse(null);
                if (inferredType != ProposalType.DELETE && Objects.nonNull(validateEntity) && !Objects.equals(validateEntity.getId(), item.getTrainingSampleId())) {
                    throw new AppException(ErrorCode.TRAINING_SAMPLE_ALREADY_EXISTS, String.format(
                            "Mẫu đào tạo đã tồn tại [Mã huấn luyện=%s, công đoạn=%s, Hạng mục=%s, Nội dung=%s, trainingSampleCode=%s]",
                            validateEntity.getTrainingCode(),
                            validateEntity.getProcess().getCode(),
                            item.getCategoryName(),
                            item.getTrainingDescription(),
                            item.getTrainingSampleCode()
                    ));
                }
                newEntity.setProposalType(inferredType);
                newEntity = trainingSampleProposalDetailRepository.save(newEntity);
                if (item.getImages() != null && !item.getImages().isEmpty()) {
                    attachmentService.uploadAttachments(item.getImages(), "TRAINING_SAMPLE_PROPOSAL", newEntity.getId(), currentUser.getUsername());
                }
                continue;
            }
            requestDetailIds.add(item.getTrainingSampleProposalDetailId());
            TrainingSampleProposalDetail entity = existingMap.get(item.getTrainingSampleProposalDetailId());
            if (entity == null) {
                throw new AppException(ErrorCode.INVALID_DETAIL_ID_FOR_PROPOSAL);
            }
            updateDetailFields(entity, item, proposal);
            trainingSampleProposalDetailRepository.save(entity);
            if (item.getImages() != null && !item.getImages().isEmpty()) {
                attachmentService.deleteAttachments("TRAINING_SAMPLE_PROPOSAL", entity.getId());
                attachmentService.uploadAttachments(item.getImages(), "TRAINING_SAMPLE_PROPOSAL", entity.getId(), currentUser.getUsername());
            }
        }
        for (TrainingSampleProposalDetail existing : existingDetails) {
            if (!requestDetailIds.contains(existing.getId())) {
                existing.setDeleteFlag(true);
                trainingSampleProposalDetailRepository.save(existing);
            }
        }
        trainingSampleProposalRepository.save(proposal);

        List<TrainingSampleProposalDetail> latestDetails = trainingSampleProposalDetailRepository.findByTrainingSampleProposalIdAndDeleteFlagFalse(id);
        List<TrainingSampleProposalDetailUpdateResponse> detailResponses = new ArrayList<>();
        for (TrainingSampleProposalDetail detail : latestDetails) {
            detailResponses.add(mapToResponse(detail));
        }

        return TrainingSampleProposalUpdateResponse.builder()
                .id(proposal.getId())
                .productLineId(proposal.getProductLine() != null ? proposal.getProductLine().getId() : null)
                .detailUpdateResponses(detailResponses)
                .build();
    }

    @Override
    @Transactional
    public void revise(Long id, User currentUser, HttpServletRequest request) {
        TrainingSampleProposal proposal = trainingSampleProposalRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_PROPOSAL_NOT_FOUND));
        if (!proposal.getCreatedBy().equals(currentUser.getUsername())) {
            throw new AppException(ErrorCode.ONLY_AUTHOR_CAN_EDIT);
        }
        createHistorySnapshot(proposal);
        approvalService.revise(proposal, currentUser, request);
    }

    private void createHistorySnapshot(TrainingSampleProposal proposal) {
        TrainingSampleProposalHistory history = TrainingSampleProposalHistory.builder()
                .trainingSampleProposal(proposal)
                .version(proposal.getCurrentVersion() == null ? 1 : proposal.getCurrentVersion())
                .recordedAt(LocalDateTime.now())
                .productLineId(proposal.getProductLine() != null ? proposal.getProductLine().getId() : null)
                .detailHistory(new ArrayList<>())
                .build();

        if (proposal.getDetails() != null) {
            for (TrainingSampleProposalDetail detail : proposal.getDetails()) {
                TrainingSampleProposalDetailHistory detailHistory = TrainingSampleProposalDetailHistory.builder()
                        .trainingSampleProposalHistory(history)
                        .trainingSampleId(detail.getTrainingSample() != null ? detail.getTrainingSample().getId() : null)
                        .proposalType(detail.getProposalType() != null ? detail.getProposalType().toString() : null)
                        .processId(detail.getProcess() != null ? detail.getProcess().getId() : null)
                        .processCode(detail.getProcess() != null ? detail.getProcess().getCode() : null)
                        .processName(detail.getProcess() != null ? detail.getProcess().getName() : null)
                        .defectId(detail.getDefect() != null ? detail.getDefect().getId() : null)
                        .categoryName(detail.getCategoryName())
                        .trainingSampleCode(detail.getTrainingSampleCode())
                        .trainingDescription(detail.getTrainingDescription())
                        .productId(detail.getProducts() != null && !detail.getProducts().isEmpty() ? detail.getProducts().iterator().next().getId() : null)
                        .note(detail.getNote())
                        .build();
                history.getDetailHistory().add(detailHistory);
            }
        }

        trainingSampleProposalHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void submit(Long proposalId, User currentUser, HttpServletRequest request) {
        TrainingSampleProposal proposal = trainingSampleProposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_PROPOSAL_NOT_FOUND));
        approvalService.submit(proposal, currentUser, request);
        trainingSampleProposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public void approve(Long proposalId, User currentUser, ApproveRequest req, HttpServletRequest request) {
        TrainingSampleProposal proposal = trainingSampleProposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_PROPOSAL_NOT_FOUND));
        approvalService.approve(proposal, currentUser, req, request);
        trainingSampleProposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public void reject(Long proposalId, User currentUser, RejectRequest req, HttpServletRequest request) {
        TrainingSampleProposal proposal = trainingSampleProposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_PROPOSAL_NOT_FOUND));
        approvalService.reject(proposal, currentUser, req, request);
        trainingSampleProposalRepository.save(proposal);
    }

    @Override
    public ResponseEntity<Boolean> canApprove(Long proposalId, User currentUser) {
        try {
            TrainingSampleProposal proposal = trainingSampleProposalRepository.findById(proposalId)
                    .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_PROPOSAL_NOT_FOUND));
            Boolean hasPermission = approvalService.canApprove(proposal, currentUser);
            return ResponseEntity.ok(hasPermission);
        } catch (AppException e) {

            return ResponseEntity.ok(Boolean.FALSE);
        }
    }

    @Override
    public void submitTrainingSampleProposalForApproval(Long proposalId, User currentUser, HttpServletRequest request) {
        TrainingSampleProposal proposal = trainingSampleProposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_PROPOSAL_NOT_FOUND));
        validateProposalForSubmission(proposal);
        approvalService.submit(proposal, currentUser, request);
        trainingSampleProposalRepository.save(proposal);
    }

    private void createDetail(List<TrainingSampleProposalDetailRequest> proposalDetailList, TrainingSampleProposal proposal, User currentUser) {
        for (TrainingSampleProposalDetailRequest detailRequest : proposalDetailList) {
            // ★ Use FE-provided proposalType if present, otherwise infer from trainingSampleId
            ProposalType resolvedType = resolveProposalType(detailRequest);

            Process process = processRepository.findById(detailRequest.getProcessId())
                    .orElseThrow(() -> new AppException(ErrorCode.PROCESS_NOT_FOUND));

            // Validate uniqueness only for CREATE and UPDATE
            List<Long> pIds = detailRequest.getProductIds() != null ? detailRequest.getProductIds() : Collections.emptyList();
            TrainingSample validateEntity = pIds.isEmpty() ? null : trainingSampleRepository.checkExist(detailRequest.getProcessId(),
                            detailRequest.getCategoryName(),
                            detailRequest.getTrainingDescription(),
                            detailRequest.getTrainingSampleCode(),
                            pIds)
                    .orElse(null);
            if (resolvedType != ProposalType.DELETE && Objects.nonNull(validateEntity) && !Objects.equals(validateEntity.getId(), detailRequest.getTrainingSampleId())) {
                throw new AppException(ErrorCode.TRAINING_SAMPLE_ALREADY_EXISTS, String.format(
                        "Mẫu đào tạo đã tồn tại [Mã huấn luyện=%s, công đoạn=%s, Hạng mục=%s, Nội dung=%s, trainingSampleCode=%s]",
                        validateEntity.getTrainingCode(),
                        validateEntity.getProcess().getCode(),
                        detailRequest.getCategoryName(),
                        detailRequest.getTrainingDescription(),
                        detailRequest.getTrainingSampleCode()
                ));
            }
            TrainingSampleProposalDetail entity = new TrainingSampleProposalDetail();

            // Handle trainingSample - can be null for CREATE
            if (detailRequest.getTrainingSampleId() != null) {
                TrainingSample trainingSample = trainingSampleRepository.findById(detailRequest.getTrainingSampleId()).orElse(null);
                entity.setTrainingSample(trainingSample);
            }

            // Handle defect - validate if not null
            if (detailRequest.getDefectId() != null) {
                Defect defect = defectRepository.findById(detailRequest.getDefectId())
                        .orElseThrow(() -> new AppException(ErrorCode.DEFECT_NOT_FOUND));
                entity.setDefect(defect);
            }

            // Handle products - validate if not null
            if (detailRequest.getProductIds() != null && !detailRequest.getProductIds().isEmpty()) {
                Set<Product> products = new HashSet<>();
                for (Long pid : detailRequest.getProductIds()) {
                    Product product = productRepository.findById(pid)
                            .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
                    products.add(product);
                }
                entity.setProducts(products);
            }

            entity.setTrainingSampleProposal(proposal);
            entity.setProposalType(resolvedType);
            entity.setCategoryName(detailRequest.getCategoryName());
            entity.setProcess(process);
            entity.setTrainingDescription(detailRequest.getTrainingDescription());
            entity.setTrainingSampleCode(detailRequest.getTrainingSampleCode());
            entity.setNote(detailRequest.getNote());

            // Save detail first to get ID for attachment
            entity = trainingSampleProposalDetailRepository.save(entity);

            // Upload images for this detail if provided
            if (detailRequest.getImages() != null && !detailRequest.getImages().isEmpty()) {
                attachmentService.uploadAttachments(detailRequest.getImages(), "TRAINING_SAMPLE_PROPOSAL", entity.getId(), currentUser.getUsername());
            }
        }

        // ★ Detect DELETE: find group members in DB not present in request
        detectAndCreateDeleteDetails(proposalDetailList, proposal);
    }

    private TrainingSampleProposalDetail mapToEntity(TrainingSampleProposalDetailRequest request, TrainingSampleProposal proposal) {
        if (request == null) throw new AppException(ErrorCode.INVALID_REQUEST_FORMAT);
        if (request.getProcessId() == null) throw new AppException(ErrorCode.MISSING_PROCESS_ID);
        if (request.getCategoryName() == null || request.getCategoryName().isBlank()) {
            throw new AppException(ErrorCode.MISSING_CATEGORY_NAME);
        }
        if (request.getTrainingDescription() == null || request.getTrainingDescription().isBlank()) {
            throw new AppException(ErrorCode.MISSING_TRAINING_DESCRIPTION);
        }

        // ★ Use FE-provided proposalType if present, otherwise infer
        ProposalType inferredType = resolveProposalType(request);

        TrainingSampleProposalDetail entity = new TrainingSampleProposalDetail();
        entity.setTrainingSampleProposal(proposal);
        entity.setProposalType(inferredType);
        entity.setProcess(processRepository.getReferenceById(request.getProcessId()));

        if (request.getTrainingSampleId() != null) {
            entity.setTrainingSample(trainingSampleRepository.getReferenceById(request.getTrainingSampleId()));
        } else {
            entity.setTrainingSample(null);
        }

        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            Set<Product> products = new HashSet<>();
            for (Long pid : request.getProductIds()) {
                products.add(productRepository.getReferenceById(pid));
            }
            entity.setProducts(products);
        }

        if (request.getDefectId() != null) {
            entity.setDefect(defectRepository.getReferenceById(request.getDefectId()));
        } else {
            entity.setDefect(null);
        }
        entity.setCategoryName(request.getCategoryName());
        entity.setTrainingSampleCode(request.getTrainingSampleCode());
        entity.setTrainingDescription(request.getTrainingDescription());
        entity.setNote(request.getNote());
        entity.setDeleteFlag(false);
        return entity;
    }

    private TrainingSampleProposalDetailUpdateResponse mapToResponse(TrainingSampleProposalDetail entity) {
        if (entity == null) return null;
        Long trainingSampleId = entity.getTrainingSample() != null ? entity.getTrainingSample().getId() : null;
        Long processId = entity.getProcess() != null ? entity.getProcess().getId() : null;
        List<Long> productIds = entity.getProducts() != null
                ? entity.getProducts().stream().map(Product::getId).collect(Collectors.toList())
                : Collections.emptyList();
        Long defectId = entity.getDefect() != null ? entity.getDefect().getId() : null;

        return TrainingSampleProposalDetailUpdateResponse.builder()
                .id(entity.getId())
                .trainingSampleId(trainingSampleId)
                .proposalType(entity.getProposalType())
                .processId(processId)
                .productIds(productIds)
                .defectId(defectId)
                .categoryName(entity.getCategoryName())
                .trainingSampleCode(entity.getTrainingSampleCode())
                .trainingDescription(entity.getTrainingDescription())
                .note(entity.getNote())
                .build();
    }

    private void validateProposalForSubmission(TrainingSampleProposal proposal) {
        if (proposal.getDetails() == null || proposal.getDetails().isEmpty()) {
            throw new AppException(ErrorCode.PROPOSAL_HAS_NO_DETAILS);
        }
        for (TrainingSampleProposalDetail detail : proposal.getDetails()) {
            if (detail.getProposalType() == null) {
                throw new AppException(ErrorCode.MISSING_PROPOSAL_TYPE);
            }
            if (detail.getProcess() == null) {
                throw new AppException(ErrorCode.MISSING_PROCESS_IN_DETAIL);
            }
            if (detail.getCategoryName() == null || detail.getCategoryName().isBlank()) {
                throw new AppException(ErrorCode.MISSING_CATEGORY_NAME);
            }
            if (detail.getTrainingDescription() == null || detail.getTrainingDescription().isBlank()) {
                throw new AppException(ErrorCode.MISSING_TRAINING_DESCRIPTION);
            }
        }
    }

    /**
     * Update existing detail fields from request, preserving non-requested fields like rejectFeedback
     */
    private void updateDetailFields(TrainingSampleProposalDetail entity, TrainingSampleProposalDetailRequest request, TrainingSampleProposal proposal) {
        if (request == null) throw new AppException(ErrorCode.INVALID_REQUEST_FORMAT);
        if (request.getProcessId() == null) throw new AppException(ErrorCode.MISSING_PROCESS_ID);
        if (request.getCategoryName() == null || request.getCategoryName().isBlank()) {
            throw new AppException(ErrorCode.MISSING_CATEGORY_NAME);
        }
        if (request.getTrainingDescription() == null || request.getTrainingDescription().isBlank()) {
            throw new AppException(ErrorCode.MISSING_TRAINING_DESCRIPTION);
        }

        // ★ Use FE-provided proposalType if present, otherwise infer
        ProposalType inferredType = resolveProposalType(request);

        entity.setTrainingSampleProposal(proposal);
        entity.setProposalType(inferredType);
        entity.setProcess(processRepository.getReferenceById(request.getProcessId()));

        if (request.getTrainingSampleId() != null) {
            entity.setTrainingSample(trainingSampleRepository.getReferenceById(request.getTrainingSampleId()));
        } else {
            entity.setTrainingSample(null);
        }

        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            Set<Product> products = new HashSet<>();
            for (Long pid : request.getProductIds()) {
                products.add(productRepository.getReferenceById(pid));
            }
            entity.setProducts(products);
        } else {
            entity.getProducts().clear();
        }

        if (request.getDefectId() != null) {
            entity.setDefect(defectRepository.getReferenceById(request.getDefectId()));
        } else {
            entity.setDefect(null);
        }

        entity.setCategoryName(request.getCategoryName());
        entity.setTrainingSampleCode(request.getTrainingSampleCode());
        entity.setTrainingDescription(request.getTrainingDescription());
        entity.setNote(request.getNote());
    }

    /**
     * Snapshot delete detection: find TrainingSample members that exist in DB groups
     * but are NOT referenced in the request → auto-create DELETE proposal details.
     * Uses 2 batch queries:
     *   Query 1: Load referenced samples to get their OLD trainingSampleCodes
     *   Query 2: Load ALL members of those old groups
     * Then compare to find missing members.
     */
    private void detectAndCreateDeleteDetails(
            List<TrainingSampleProposalDetailRequest> requestDetails,
            TrainingSampleProposal proposal) {

        // Collect trainingSampleIds from request (non-null only = existing records)
        Set<Long> requestIds = requestDetails.stream()
                .map(TrainingSampleProposalDetailRequest::getTrainingSampleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (requestIds.isEmpty()) return;

        // ① Query 1: Batch load referenced samples → get OLD trainingSampleCodes
        List<TrainingSample> referenced = trainingSampleRepository.findAllById(requestIds);
        Set<String> oldCodes = referenced.stream()
                .map(TrainingSample::getTrainingSampleCode)
                .filter(code -> code != null && !code.isBlank())
                .collect(Collectors.toSet());

        if (oldCodes.isEmpty()) return;

        // ② Query 2: Batch load ALL members of old groups
        List<TrainingSample> allGroupMembers = trainingSampleRepository
                .findByTrainingSampleCodeInAndProductLineIdAndDeleteFlagFalse(
                        oldCodes, proposal.getProductLine().getId());

        // ③ Compare: members in DB but not in request → CREATE DELETE details
        for (TrainingSample member : allGroupMembers) {
            if (!requestIds.contains(member.getId())) {
                TrainingSampleProposalDetail deleteDetail = new TrainingSampleProposalDetail();
                deleteDetail.setTrainingSampleProposal(proposal);
                deleteDetail.setTrainingSample(member);
                deleteDetail.setProposalType(ProposalType.DELETE);
                deleteDetail.setProcess(member.getProcess());
                deleteDetail.setProducts(new HashSet<>(member.getProducts()));
                deleteDetail.setDefect(member.getDefect());
                deleteDetail.setCategoryName(member.getCategoryName());
                deleteDetail.setTrainingSampleCode(member.getTrainingSampleCode());
                deleteDetail.setTrainingDescription(member.getTrainingDescription());
                deleteDetail.setNote(member.getNote());
                deleteDetail.setDeleteFlag(false);
                trainingSampleProposalDetailRepository.save(deleteDetail);
                log.info("Auto-created DELETE detail for TrainingSample id={} (not in request snapshot)", member.getId());
            }
        }
    }

    /**
     * Resolve ProposalType: use FE-provided value if present, otherwise infer from trainingSampleId.
     * - FE sends explicit proposalType (e.g., DELETE for deleting entire group) → use it
     * - FE sends null → infer: trainingSampleId == null → CREATE, otherwise → UPDATE
     */
    private ProposalType resolveProposalType(TrainingSampleProposalDetailRequest request) {
        if (request.getProposalType() != null) {
            return request.getProposalType();
        }
        return (request.getTrainingSampleId() == null)
                ? ProposalType.CREATE
                : ProposalType.UPDATE;
    }
}