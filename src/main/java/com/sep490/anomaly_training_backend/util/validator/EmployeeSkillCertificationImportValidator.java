package com.sep490.anomaly_training_backend.util.validator;

import com.sep490.anomaly_training_backend.dto.EmployeeSkillCertificationImportDto;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmployeeSkillCertificationImportValidator {

    /**
     * Validate all parsed rows - ONLY file-level validation
     */
    public void validateFileData(List<EmployeeSkillCertificationImportDto> parsedRows, List<ImportErrorItem> errors) {
        if (parsedRows == null || parsedRows.isEmpty()) {
            errors.add(ImportErrorItem.builder()
                    .field("SYSTEM")
                    .message("File contains no data rows")
                    .build());
            return;
        }

        validateRequiredFields(parsedRows, errors);
        validateQuantityRange(parsedRows, errors);
        validateUniqueKey(parsedRows, errors);

        log.info("File validation completed. Total rows: {}, Errors: {}", parsedRows.size(), errors.size());
    }

    /**
     * Validate required fields
     */
    private void validateRequiredFields(List<EmployeeSkillCertificationImportDto> parsedRows, List<ImportErrorItem> errors) {
        for (EmployeeSkillCertificationImportDto row : parsedRows) {
            int rowNum = row.getExcelRowNumber();

            if (isBlank(row.getSectionCode())) {
                errors.add(buildRowError(rowNum, "sectionCode", null, "Section code is required"));
            }

            if (isBlank(row.getProductLineCode())) {
                errors.add(buildRowError(rowNum, "productLineCode", null, "Product line code is required"));
            }

            if (isBlank(row.getProcessName())) {
                errors.add(buildRowError(rowNum, "processName", null, "Process name is required"));
            }

            if (isBlank(row.getEmployeeId())) {
                errors.add(buildRowError(rowNum, "employeeId", null, "Employee ID is required"));
            }
        }
    }

    /**
     * Validate certified quantity > 0
     */
    private void validateQuantityRange(List<EmployeeSkillCertificationImportDto> parsedRows, List<ImportErrorItem> errors) {
        for (EmployeeSkillCertificationImportDto row : parsedRows) {
            if (row.getCertifiedQuantity() != null && row.getCertifiedQuantity() <= 0) {
                errors.add(buildRowError(
                        row.getExcelRowNumber(),
                        "certifiedQuantity",
                        String.valueOf(row.getCertifiedQuantity()),
                        "Certified quantity must be greater than 0"
                ));
            }
        }
    }

    /**
     * Validate unique key: (sectionCode, productLineCode, processName, employeeId)
     */
    private void validateUniqueKey(List<EmployeeSkillCertificationImportDto> parsedRows, List<ImportErrorItem> errors) {
        Map<String, Integer> firstRowOfKey = new HashMap<>();

        for (EmployeeSkillCertificationImportDto row : parsedRows) {
            if (isBlank(row.getSectionCode())
                    || isBlank(row.getProductLineCode())
                    || isBlank(row.getProcessName())
                    || isBlank(row.getEmployeeId())) {
                continue;
            }

            String key = buildUniqueKey(
                    row.getSectionCode(),
                    row.getProductLineCode(),
                    row.getProcessName(),
                    row.getEmployeeId()
            );

            Integer firstRow = firstRowOfKey.get(key);
            if (firstRow == null) {
                firstRowOfKey.put(key, row.getExcelRowNumber());
            } else {
                errors.add(buildRowError(
                        row.getExcelRowNumber(),
                        "DUPLICATE_KEY",
                        null,
                        "Duplicate (sectionCode, productLineCode, processName, employeeId) found. " +
                                "First occurrence at row " + firstRow
                ));
            }
        }
    }

    private String buildUniqueKey(String sectionCode, String productLineCode, String processName, String employeeId) {
        return normalize(sectionCode) + "|" +
                normalize(productLineCode) + "|" +
                normalize(processName) + "|" +
                normalize(employeeId);
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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