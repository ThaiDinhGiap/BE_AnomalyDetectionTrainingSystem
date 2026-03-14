package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.TrainingSampleImportDto;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleResponse;
import com.sep490.anomaly_training_backend.enums.ImportStatus;
import com.sep490.anomaly_training_backend.enums.ImportType;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleMapper;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.TrainingSampleService;
import com.sep490.anomaly_training_backend.service.ImportHistoryService;
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

    @Override
    public List<TrainingSampleResponse> getTrainingSampleByProductLine(Long productLineId) {
        List<TrainingSample> listEntity = trainingSampleRepository.findByProductLineIdAndDeleteFlagFalse(productLineId);
        return listEntity.stream().map(trainingSampleMapper::toDto).toList();
    }

    @Override
    public TrainingSampleResponse getTrainingSampleById(Long id) {
        TrainingSample entity = trainingSampleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_NOT_FOUND));
        return trainingSampleMapper.toDto(entity);
    }

    @Override
    public List<TrainingSampleResponse> getTrainingSampleByProcess(Long id) {
        TrainingSample entity = trainingSampleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_NOT_FOUND));
        return trainingSampleRepository.findByProcessIdAndDeleteFlagFalse(entity.getProcess().getId())
                .stream().map(trainingSampleMapper::toDto).toList();
    }

    @Override
    public List<TrainingSampleResponse> getTrainingSampleByCategory(Long id) {
        TrainingSample entity = trainingSampleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_NOT_FOUND));
        return trainingSampleRepository.findByCategoryNameAndDeleteFlagFalse(entity.getCategoryName())
                .stream().map(trainingSampleMapper::toDto).toList();
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
                    saveImportFailHistory(currentUser, file, errors);
                    throw new AppException(ErrorCode.PRODUCT_LINE_NOT_IN_HEADER);
                }

                List<TrainingSampleImportDto> parsedRows = importHelper.parseExcelRowsWithCarryForward(sheet, errors);

                if (!errors.isEmpty()) {
                    saveImportFailHistory(currentUser, file, errors);
                    throw new AppException(ErrorCode.IMPORT_PARSE_ERROR);
                }

                // Step 4: Validate only file data (NO DB validation)
                importValidator.validateFileData(parsedRows, errors);

                if (!errors.isEmpty()) {
                    saveImportFailHistory(currentUser, file, errors);
                    throw new AppException(ErrorCode.IMPORT_VALIDATION_ERROR);
                }

                // Step 5: Upsert all rows (changed from insert to upsert)
                List<TrainingSampleResponse> responses = upsertAllRows(parsedRows, productLine, sheet);

                // Step 6: Save success history
                saveImportPassHistory(currentUser, file);

                return responses;

            } catch (AppException e) {
                throw e;
            } catch (Exception e) {
                log.error("Import training sample failed", e);
                if (errors.isEmpty()) {
                    errors.add(buildSystemError("System error: " + e.getMessage()));
                    saveImportFailHistory(currentUser, file, errors);
                }
                throw new AppException(ErrorCode.CANNOT_READ_EXCEL_FILE);
            }

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during import", e);
            throw new AppException(ErrorCode.UNEXPECTED_IMPORT_ERROR);
        }
    }

    /**
     * Upsert all parsed rows into database
     * - Find by trainingCode
     * - Update if exists OR Create if new
     * - Handle images from Excel rows
     */
    private List<TrainingSampleResponse> upsertAllRows(
            List<TrainingSampleImportDto> parsedRows,
            ProductLine productLine,
            Sheet sheet) {

        List<TrainingSampleResponse> responses = new ArrayList<>();

        for (int rowIndex = 0; rowIndex < parsedRows.size(); rowIndex++) {
            TrainingSampleImportDto dto = parsedRows.get(rowIndex);
            
            try {
                // Upsert entity
                TrainingSample sample = upsertTrainingSample(dto, productLine);

                // Handle images from Excel row (if any)
                // rowIndex + 3 because: row 1 = header, row 2 = column names, row 3 onwards = data
                int excelRowIndex = rowIndex + 3;
                handleTrainingSampleImages(sheet, sample, excelRowIndex);

                responses.add(trainingSampleMapper.toDto(sample));

            } catch (Exception e) {
                log.error("Error upserting training sample with code {}: {}", dto.getTrainingCode(), e.getMessage(), e);
                // Continue với rows khác, không throw exception
            }
        }

        return responses;
    }

    /**
     * Upsert one TrainingSample by trainingCode
     * - Find by trainingCode
     * - If exists: update all fields
     * - If not exists: create new
     */
    private TrainingSample upsertTrainingSample(
            TrainingSampleImportDto dto,
            ProductLine productLine) {

        // Find existing by trainingCode
        TrainingSample sample = trainingSampleRepository
                .findByTrainingCode(dto.getTrainingCode())
                .orElseGet(TrainingSample::new);

        // Resolve relationships
        Process process = null;
        if (dto.getProcessCode() != null) {
            process = processRepository.findByCode(dto.getProcessCode()).orElse(null);
        }

        Defect defect = null;
        if (dto.getDefectCode() != null) {
            defect = defectRepository.findByDefectCode(dto.getDefectCode()).orElse(null);
        }

        Product product = null;
        if (dto.getProductCode() != null) {
            product = productRepository.findByCode(dto.getProductCode()).orElse(null);
        }

        // Update all fields (for both insert and update scenarios)
        sample.setTrainingCode(dto.getTrainingCode());
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

        TrainingSample saved = trainingSampleRepository.save(sample);

        log.info("Upserted TrainingSample: code={}, id={}, isNew={}", 
                dto.getTrainingCode(), saved.getId(), sample.getId() == null);

        return saved;
    }

    /**
     * Handle images for TrainingSample from Excel row
     * - Extract images từ row
     * - Delete old attachments (if updating existing record)
     * - Upload new images
     */
    private void handleTrainingSampleImages(Sheet sheet, TrainingSample sample, int excelRowIndex) {
        try {
            if (sample.getId() == null) {
                log.debug("Sample has no ID, skipping image handling");
                return;
            }

            Row row = sheet.getRow(excelRowIndex);
            if (row == null) {
                log.debug("Row {} is null, skipping image handling", excelRowIndex);
                return;
            }

            log.info("Handling images for TrainingSample id={} from row {}", sample.getId(), excelRowIndex);

            // Note: Image handling service is injected and will be called here
            // Currently images are not extracted from Excel in this import
            // Future: If Excel format includes images, call importImageHandlerService here
            // importImageHandlerService.handleRowImages(row, "TRAINING_SAMPLE", sample.getId(), "SYSTEM");

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

            String productLineName = getCellStringValue(cell);
            if (productLineName == null || productLineName.trim().isEmpty()) {
                return null;
            }

            return productLineRepository.findByName(productLineName.trim()).orElse(null);
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
}