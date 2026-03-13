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
import com.sep490.anomaly_training_backend.service.ImportHistoryService;
import com.sep490.anomaly_training_backend.service.TrainingSampleService;
import com.sep490.anomaly_training_backend.util.import_helper.TrainingSampleImportHelper;
import com.sep490.anomaly_training_backend.util.import_helper.TrainingSampleImportValidator;
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

                importValidator.validateFileData(parsedRows, errors);

                if (!errors.isEmpty()) {
                    saveImportFailHistory(currentUser, file, errors);
                    throw new AppException(ErrorCode.IMPORT_VALIDATION_ERROR);
                }

                List<TrainingSampleResponse> responses = insertAllRows(parsedRows, productLine);

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

    private List<TrainingSampleResponse> insertAllRows(
            List<TrainingSampleImportDto> parsedRows,
            ProductLine productLine) {

        List<TrainingSampleResponse> responses = new ArrayList<>();

        for (TrainingSampleImportDto dto : parsedRows) {
            Process process = processRepository.findByCode(dto.getProcessCode()).orElse(null);
            Defect defect = dto.getDefectCode() != null ? defectRepository.findByDefectCode(dto.getDefectCode()).orElse(null) : null;
            Product product = dto.getProductCode() != null ? productRepository.findByCode(dto.getProductCode()).orElse(null) : null;

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