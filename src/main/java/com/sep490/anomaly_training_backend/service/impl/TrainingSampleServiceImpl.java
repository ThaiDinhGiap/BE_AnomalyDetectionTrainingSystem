package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.TrainingSampleImportDto;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleResponse;
import com.sep490.anomaly_training_backend.enums.ImportType;
import com.sep490.anomaly_training_backend.enums.ImportStatus;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleMapper;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.TrainingSampleService;
import com.sep490.anomaly_training_backend.service.ImportHistoryService;
import com.sep490.anomaly_training_backend.util.import_helper.TrainingSampleImportHelper;
import com.sep490.anomaly_training_backend.util.import_helper.TrainingSampleImportValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
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
            .orElseThrow(() -> new EntityNotFoundException("Training Sample not found"));
        return trainingSampleMapper.toDto(entity);
    }

    @Override
    public List<TrainingSampleResponse> getTrainingSampleByProcess(Long id) {
        TrainingSample entity = trainingSampleRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Training Sample not found"));
        return trainingSampleRepository.findByProcessIdAndDeleteFlagFalse(entity.getProcess().getId())
            .stream().map(trainingSampleMapper::toDto).toList();
    }

    @Override
    public List<TrainingSampleResponse> getTrainingSampleByCategory(Long id) {
        TrainingSample entity = trainingSampleRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Training Sample not found"));
        return trainingSampleRepository.findByCategoryNameAndDeleteFlagFalse(entity.getCategoryName())
            .stream().map(trainingSampleMapper::toDto).toList();
    }

    @Override
    public List<TrainingSampleResponse> importTrainingSample(User currentUser, MultipartFile file) throws BadRequestException {
        List<ImportErrorItem> errors = new ArrayList<>();
        try {
            // Step 1: Validate file format
            validateImportFile(file);

            try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
                Sheet sheet = getFirstSheet(workbook);

                // Step 2: Get ProductLine from row 1
                ProductLine productLine = getProductLineFromHeader(sheet);
                if (productLine == null) {
                    errors.add(buildSystemError("ProductLine not found in file header (Row 1)"));
                    saveImportFailHistory(currentUser, file, errors);
                    throw new BadRequestException("ProductLine not found in file header");
                }

                // Step 3: Parse all rows with carry-forward logic
                List<TrainingSampleImportDto> parsedRows = importHelper.parseExcelRowsWithCarryForward(sheet, errors);

                if (!errors.isEmpty()) {
                    saveImportFailHistory(currentUser, file, errors);
                    throw new BadRequestException("Error parsing rows. Please check import history.");
                }

                // Step 4: Validate only file data (NO DB validation)
                importValidator.validateFileData(parsedRows, errors);

                if (!errors.isEmpty()) {
                    saveImportFailHistory(currentUser, file, errors);
                    throw new BadRequestException("Validation failed. Please check import history.");
                }

                // Step 5: Insert all rows
                List<TrainingSampleResponse> responses = insertAllRows(parsedRows, productLine);

                // Step 6: Save success history
                saveImportPassHistory(currentUser, file);

                return responses;

            } catch (BadRequestException e) {
                if (errors.isEmpty()) {
                    errors.add(buildSystemError(e.getMessage()));
                    saveImportFailHistory(currentUser, file, errors);
                }
                throw e;
            } catch (Exception e) {
                log.error("Import training sample failed", e);
                if (errors.isEmpty()) {
                    errors.add(buildSystemError("System error: " + e.getMessage()));
                    saveImportFailHistory(currentUser, file, errors);
                }
                throw new BadRequestException("Cannot read excel file: " + e.getMessage());
            }

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during import", e);
            throw new BadRequestException("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Insert all parsed rows into database (simple insert, no upsert)
     */
    private List<TrainingSampleResponse> insertAllRows(
            List<TrainingSampleImportDto> parsedRows,
            ProductLine productLine) throws BadRequestException {

        List<TrainingSampleResponse> responses = new ArrayList<>();

        for (TrainingSampleImportDto dto : parsedRows) {
            // Resolve entities from codes (just lookup, no validation error handling)
            Process process = processRepository.findByCode(dto.getProcessCode()).orElse(null);
            Defect defect = dto.getDefectCode() != null ? defectRepository.findByDefectCode(dto.getDefectCode()).orElse(null) : null;
            Product product = dto.getProductCode() != null ? productRepository.findByCode(dto.getProductCode()).orElse(null) : null;

            // Create new TrainingSample
            TrainingSample sample = TrainingSample.builder()
                .trainingCode(dto.getTrainingCode())
                .process(process)
                .productLine(productLine)
                .categoryName(dto.getCategoryName())
                .trainingDescription(dto.getTrainingDescription())
                .trainingSampleCode(dto.getTrainingSampleCode())
                .defect(defect)
                .product(product)
                .processOrder(dto.getProcessOrder())
                .categoryOrder(dto.getCategoryOrder())
                .contentOrder(dto.getContentOrder())
                .note(dto.getNote())
                .build();

            TrainingSample saved = trainingSampleRepository.save(sample);
            responses.add(trainingSampleMapper.toDto(saved));
        }

        return responses;
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

    private void validateImportFile(MultipartFile file) throws BadRequestException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null ||
                (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls"))) {
            throw new BadRequestException("Only .xls or .xlsx files are supported");
        }
    }

    private Sheet getFirstSheet(Workbook workbook) throws BadRequestException {
        if (workbook.getNumberOfSheets() == 0) {
            throw new BadRequestException("Excel file does not contain any sheet");
        }

        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            throw new BadRequestException("Cannot read first sheet");
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

