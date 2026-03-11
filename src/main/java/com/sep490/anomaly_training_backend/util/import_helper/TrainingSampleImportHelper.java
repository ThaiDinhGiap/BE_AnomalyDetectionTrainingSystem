package com.sep490.anomaly_training_backend.util.import_helper;

import com.sep490.anomaly_training_backend.dto.request.TrainingSampleImportDto;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleImportRowData;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Helper class to parse TrainingSample Excel import
 * ONLY responsible for:
 * - Parse Excel rows into structured data
 * - Apply carry-forward logic (process, category, defect inheritance)
 * - Calculate order fields (processOrder, categoryOrder, contentOrder)
 *
 * Does NOT validate against database
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TrainingSampleImportHelper {

    // Excel column indices (0-based)
    private static final int COL_PROCESS = 1;
    private static final int COL_CATEGORY = 2;
    private static final int COL_DEFECT = 3;
    private static final int COL_TRAINING_DESCRIPTION = 4;
    private static final int COL_TRAINING_SAMPLE_CODE = 5;
    private static final int COL_PRODUCT_CODE = 6;
    private static final int COL_TRAINING_CODE =7;
    private static final int COL_NOTE = 8;

    /**
     * Parse Excel file starting from row 3
     * Apply carry-forward logic and calculate order fields
     */
    public List<TrainingSampleImportDto> parseExcelRowsWithCarryForward(
            Sheet sheet,
            List<ImportErrorItem> errors) {

        List<TrainingSampleImportRowData> rawRows = new ArrayList<>();

        // Step 1: Parse all raw rows
        for (int i = 2; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            if (isRowEmpty(row)) {
                continue;
            }

            try {
                TrainingSampleImportRowData rowData = parseRawRow(row, i + 1);
                rawRows.add(rowData);
            } catch (Exception e) {
                errors.add(buildRowError(i + 1, "ROW", null, "Error parsing row: " + e.getMessage()));
            }
        }

        // Step 2: Apply carry-forward logic and build resolved DTOs
        List<TrainingSampleImportDto> resolvedRows = new ArrayList<>();
        String currentProcess = null;
        String currentCategory = null;
        String currentDefect = null;

        int processOrderCounter = 1;
        String lastProcessCode = null;
        Map<String, Integer> categoryOrderMap = new HashMap<>();
        Map<String, Integer> contentOrderMap = new HashMap<>();

        for (TrainingSampleImportRowData rawRow : rawRows) {
            // Update carry-forward values
            if (isNotBlank(rawRow.getProcessColumn())) {
                currentProcess = rawRow.getProcessColumn().trim();
                lastProcessCode = currentProcess;
                processOrderCounter++;
                categoryOrderMap.clear();
                contentOrderMap.clear();
            }

            if (isNotBlank(rawRow.getCategoryColumn())) {
                currentCategory = rawRow.getCategoryColumn().trim();
                String categoryKey = lastProcessCode + "|" + currentCategory;
                categoryOrderMap.put(categoryKey, categoryOrderMap.getOrDefault(categoryKey, 0) + 1);
                contentOrderMap.put(categoryKey, 0);
            }

            if (isNotBlank(rawRow.getDefectColumn())) {
                currentDefect = rawRow.getDefectColumn().trim();
            }

            // Determine if this is header row or content row
            String trainingDesc = rawRow.getTrainingDescriptionColumn();
            boolean isHeaderRow = isBlank(trainingDesc);

            // Skip pure header rows (don't add to resolved list)
            if (isHeaderRow) {
                continue;
            }

            // Build resolved DTO
            String categoryKey = lastProcessCode + "|" + currentCategory;
            Integer categoryOrder = categoryOrderMap.getOrDefault(categoryKey, 1);
            Integer contentOrder = contentOrderMap.getOrDefault(categoryKey, 0) + 1;
            contentOrderMap.put(categoryKey, contentOrder);

            TrainingSampleImportDto resolvedDto = TrainingSampleImportDto.builder()
                .processCode(currentProcess)
                .categoryName(currentCategory)
                .defectCode(currentDefect)
                .trainingDescription(trainingDesc.trim())
                .trainingSampleCode(normalize(rawRow.getTrainingSampleCodeColumn()))
                .productCode(normalize(rawRow.getProductCodeColumn()))
                .trainingCode(normalize(rawRow.getTrainingCodeColumn()))
                .processOrder(lastProcessCode != null ? processOrderCounter : 1)
                .categoryOrder(categoryOrder)
                .contentOrder(contentOrder)
                .excelRowNumber(rawRow.getExcelRowNumber())
                .note(normalize(rawRow.getNoteColumn()))
                .isHeaderRow(false)
                .build();

            resolvedRows.add(resolvedDto);
        }

        return resolvedRows;
    }

    /**
     * Parse raw Excel row into TrainingSampleImportRowData
     */
    private TrainingSampleImportRowData parseRawRow(Row row, int excelRowNumber) {
        String processColumn = getOptionalStringCellValue(row.getCell(COL_PROCESS));
        String categoryColumn = getOptionalStringCellValue(row.getCell(COL_CATEGORY));
        String defectColumn = getOptionalStringCellValue(row.getCell(COL_DEFECT));
        String trainingDescriptionColumn = getOptionalStringCellValue(row.getCell(COL_TRAINING_DESCRIPTION));
        String trainingSampleCodeColumn = getOptionalStringCellValue(row.getCell(COL_TRAINING_SAMPLE_CODE));
        String productCodeColumn = getOptionalStringCellValue(row.getCell(COL_PRODUCT_CODE));
        String noteColumn = getOptionalStringCellValue(row.getCell(COL_NOTE));
        String trainingCodeColumn = getOptionalStringCellValue(row.getCell(COL_TRAINING_CODE));

        return TrainingSampleImportRowData.builder()
            .processColumn(processColumn)
            .categoryColumn(categoryColumn)
            .defectColumn(defectColumn)
            .trainingDescriptionColumn(trainingDescriptionColumn)
            .trainingSampleCodeColumn(trainingSampleCodeColumn)
            .productCodeColumn(productCodeColumn)
            .noteColumn(noteColumn)
            .trainingCodeColumn(trainingCodeColumn)
            .excelRowNumber(excelRowNumber)
            .isEmpty(false)
            .isHeaderRow(isBlank(trainingDescriptionColumn))
            .build();
    }

    // ============= Cell Reading Utilities =============

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int c = 0; c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellDisplayValue(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    private String getOptionalStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case STRING -> normalize(cell.getStringCellValue());
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }

                double value = cell.getNumericCellValue();
                if (value == (long) value) {
                    yield String.valueOf((long) value);
                }
                yield String.valueOf(value);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> normalize(cell.toString());
            case BLANK -> null;
            default -> normalize(cell.toString());
        };
    }

    private String getCellDisplayValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }

                double value = cell.getNumericCellValue();
                if (value == (long) value) {
                    yield String.valueOf((long) value);
                }
                yield String.valueOf(value);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.toString();
            case BLANK -> "";
            default -> cell.toString();
        };
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    private ImportErrorItem buildRowError(Integer rowNumber, String field, String value, String message) {
        return ImportErrorItem.builder()
            .rowNumber(rowNumber)
            .field(field)
            .value(value)
            .message(message)
            .build();
    }
}
