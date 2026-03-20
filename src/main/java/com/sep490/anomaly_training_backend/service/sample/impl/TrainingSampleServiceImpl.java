package com.sep490.anomaly_training_backend.service.sample.impl;

import com.sep490.anomaly_training_backend.dto.request.ImageData;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleImportDto;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import com.sep490.anomaly_training_backend.dto.response.sample.CategorySample;
import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleResponse;
import com.sep490.anomaly_training_backend.enums.ImportStatus;
import com.sep490.anomaly_training_backend.enums.ImportType;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleMapper;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.ProductService;
import com.sep490.anomaly_training_backend.service.sample.TrainingSampleService;
import com.sep490.anomaly_training_backend.service.ImportHistoryService;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import com.sep490.anomaly_training_backend.service.minio.ImportImageHandlerService;
import com.sep490.anomaly_training_backend.util.TrainingCodeGenerator;
import com.sep490.anomaly_training_backend.util.helper.TrainingSampleImportHelper;
import com.sep490.anomaly_training_backend.util.validator.TrainingSampleImportValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Override
    public List<TrainingSampleResponse> getTrainingSampleByProductLine(Long productLineId) {
        return trainingSampleRepository.findByProductLineIdAndDeleteFlagFalse(productLineId)
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
        return trainingSampleRepository.findByProcessIdAndDeleteFlagFalse(entity.getProcess().getId())
                .stream().map(this::enrichResponse).toList();
    }

    @Override
    public List<TrainingSampleResponse> getTrainingSampleByCategory(Long id) {
        TrainingSample entity = trainingSampleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_NOT_FOUND));
        return trainingSampleRepository.findByCategoryNameAndDeleteFlagFalse(entity.getCategoryName())
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
            process = processRepository.findByProductLineCodeAndCode(productLine.getCode(),dto.getProcessCode())
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
                            dto.getProductCode(),
                            "Product not found with code: " + dto.getProductCode(),
                            ErrorCode.DEFECT_NOT_FOUND
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
        sample.setProcess(process);
        sample.setProductLine(productLine);
        sample.setCategoryName(dto.getCategoryName());
        sample.setTrainingDescription(dto.getTrainingDescription());
        sample.setTrainingSampleCode(dto.getTrainingSampleCode());
        sample.setDefect(defect);
        sample.setProduct(product);
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

    private void validateImportFile(MultipartFile file)  {
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
        if (entity.getProduct() != null) {
            response.setProduct(productService.getProductById(entity.getProduct().getId()));
        }
        return addAttachment(response);
    }
}