package com.sep490.anomaly_training_backend.util.validator;

import com.sep490.anomaly_training_backend.dto.request.TrainingSampleImportDto;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Validator for TrainingSample import - validates ONLY file data
 * Does NOT validate against database
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TrainingSampleImportValidator {

    /**
     * Validate all parsed rows - ONLY file-level validation
     * Rules:
     * - Content rows: processCode, categoryName, trainingDescription, trainingCode are required
     * - Header rows: processCode, categoryName are required; trainingDescription, trainingCode optional
     * - No duplicate trainingCode within content rows
     * - No duplicate business key: (processCode, categoryName, trainingDescription, trainingSampleCode, productCode)
     */
    public void validateFileData(List<TrainingSampleImportDto> parsedRows, List<ImportErrorItem> errors) {
        validateRequiredFields(parsedRows, errors);
//        validateTrainingCodeRules(parsedRows, errors);
        validateBusinessKeyDuplicates(parsedRows, errors);
    }

    /**
     * Validate required fields per row type
     */
    private void validateRequiredFields(List<TrainingSampleImportDto> parsedRows, List<ImportErrorItem> errors) {
        for (TrainingSampleImportDto row : parsedRows) {
            boolean isHeaderRow = row.getIsHeaderRow() != null && row.getIsHeaderRow();

            // Required for both header and content rows
            if (isBlank(row.getProcessCode())) {
                errors.add(buildRowError(row.getExcelRowNumber(), "processCode", null, "processCode is required"));
            }

            if (isBlank(row.getCategoryName())) {
                errors.add(buildRowError(row.getExcelRowNumber(), "categoryName", null, "categoryName is required"));
            }

            // Required only for content rows
            if (!isHeaderRow) {
                if (isBlank(row.getTrainingDescription())) {
                    errors.add(buildRowError(row.getExcelRowNumber(), "trainingDescription", null, "trainingDescription is required"));
                }
            }
        }
    }

//    /**
//     * Validate trainingCode rules
//     * - Content rows: trainingCode must not exceed 20 chars
//     * - Content rows: trainingCode must not duplicate
//     */
//    private void validateTrainingCodeRules(List<TrainingSampleImportDto> parsedRows, List<ImportErrorItem> errors) {
//        Map<String, Integer> firstRowOfCode = new HashMap<>();
//
//        for (TrainingSampleImportDto row : parsedRows) {
//            boolean isHeaderRow = Boolean.TRUE.equals(row.getIsHeaderRow());
//            if (isHeaderRow) {
//                continue;
//            }
//
//            String code = normalize(row.getTrainingCode());
//
//            // bỏ qua blank vì đã có rule required xử lý
//            if (code.isEmpty()) {
//                continue;
//            }
//
//            // Check length
//            if (code.length() > 20) {
//                errors.add(buildRowError(
//                        row.getExcelRowNumber(),
//                        "trainingCode",
//                        code,
//                        "trainingCode must not exceed 20 characters"
//                ));
//                continue;
//            }
//
//            Integer firstRow = firstRowOfCode.get(code);
//            if (firstRow == null) {
//                firstRowOfCode.put(code, row.getExcelRowNumber());
//            } else {
//                errors.add(buildRowError(
//                        row.getExcelRowNumber(),
//                        "trainingCode",
//                        code,
//                        "trainingCode " + code + " is duplicated with row " + firstRow
//                ));
//            }
//        }
//    }

    /**
     * Validate business key duplicates
     * Key: (processCode, categoryName, trainingDescription, trainingSampleCode, productCode)
     * Only for content rows
     */
    private void validateBusinessKeyDuplicates(List<TrainingSampleImportDto> parsedRows, List<ImportErrorItem> errors) {
        Map<String, Integer> firstRowOfKey = new HashMap<>();

        for (TrainingSampleImportDto row : parsedRows) {
            boolean isHeaderRow = Boolean.TRUE.equals(row.getIsHeaderRow());
            if (isHeaderRow) {
                continue;
            }

            // bỏ qua những row chưa đủ dữ liệu bắt buộc để tránh báo lỗi chồng chéo
            if (isBlank(row.getProcessCode())
                    || isBlank(row.getCategoryName())
                    || isBlank(row.getTrainingDescription())) {
                continue;
            }

            String key = buildBusinessKey(
                    row.getProcessCode(),
                    row.getCategoryName(),
                    row.getTrainingDescription(),
                    row.getTrainingSampleCode(),
                    row.getProductCode()
            );

            Integer firstRow = firstRowOfKey.get(key);
            if (firstRow == null) {
                firstRowOfKey.put(key, row.getExcelRowNumber());
            } else {
                errors.add(buildRowError(
                        row.getExcelRowNumber(),
                        "DUPLICATE_BUSINESS_KEY",
                        null,
                        "Row " + row.getExcelRowNumber()
                                + " is duplicated with row " + firstRow
                                + " by (processCode, categoryName, trainingDescription, trainingSampleCode, productCode)"
                ));
            }
        }
    }

    /**
     * Build business key from 5 fields
     * Normalize all fields first (trim, null -> blank)
     */
    private String buildBusinessKey(
            String processCode,
            String categoryName,
            String trainingDescription,
            String trainingSampleCode,
            String productCode) {

        return normalize(processCode) + "|" +
               normalize(categoryName) + "|" +
               normalize(trainingDescription) + "|" +
               normalize(trainingSampleCode) + "|" +
               normalize(productCode);
    }

    /**
     * Normalize value: trim, null/blank -> empty string
     */
    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        return value.trim();
    }

    /**
     * Check if blank (null or empty after trim)
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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
}

