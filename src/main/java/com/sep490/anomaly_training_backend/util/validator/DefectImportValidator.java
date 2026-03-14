package com.sep490.anomaly_training_backend.util.validator;

import com.sep490.anomaly_training_backend.dto.request.DefectImportDto;
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
/**
 * Validator cho import Defect - chỉ validate dữ liệu trong file
 * KHÔNG validate database
 */
public class DefectImportValidator {
    /**
     * Validate toàn bộ parsed rows - chỉ validate file-level
     *
     * Rule:
     * - defectCode là bắt buộc
     * - processCode là bắt buộc
     * - defectDescription là bắt buộc
     * - defectCode không được trùng trong cùng file
     * - defectDescription không được trùng trong cùng file
     */
    public void validateFileData(List<DefectImportDto> parsedRows, List<ImportErrorItem> errors) {
        validateRequiredFields(parsedRows, errors);
        validateDefectCodeDuplicates(parsedRows, errors);
        validateDefectDescriptionDuplicates(parsedRows, errors);
    }

    /**
     * Validate các field bắt buộc
     */
    private void validateRequiredFields(List<DefectImportDto> parsedRows, List<ImportErrorItem> errors) {
        for (DefectImportDto row : parsedRows) {
            if (isBlank(row.getProcessCode())) {
                errors.add(buildRowError(
                        row.getExcelRowNumber(),
                        "processCode",
                        null,
                        "processCode is required"
                ));
            }

            if (isBlank(row.getDefectDescription())) {
                errors.add(buildRowError(
                        row.getExcelRowNumber(),
                        "defectDescription",
                        null,
                        "defectDescription is required"
                ));
            }
        }
    }

    /**
     * Validate defectCode không được trùng trong cùng file
     */
    private void validateDefectCodeDuplicates(List<DefectImportDto> parsedRows, List<ImportErrorItem> errors) {
        Map<String, Integer> defectCodeCount = new HashMap<>();
        Map<String, Integer> firstRowOfCode = new HashMap<>();

        for (DefectImportDto row : parsedRows) {
            String defectCode = normalize(row.getDefectCode());
            if (defectCode == null) {
                continue;
            }

            defectCodeCount.put(defectCode, defectCodeCount.getOrDefault(defectCode, 0) + 1);
            if (!firstRowOfCode.containsKey(defectCode)) {
                firstRowOfCode.put(defectCode, row.getExcelRowNumber());
            }
        }

        for (Map.Entry<String, Integer> entry : defectCodeCount.entrySet()) {
            if (entry.getValue() > 1) {
                String defectCode = entry.getKey();
                Integer firstRow = firstRowOfCode.get(defectCode);

                for (DefectImportDto row : parsedRows) {
                    if (defectCode.equals(normalize(row.getDefectCode()))) {
                        errors.add(buildRowError(
                                row.getExcelRowNumber(),
                                "defectCode",
                                defectCode,
                                "defectCode " + defectCode + " is duplicated with row " + firstRow
                        ));
                    }
                }
            }
        }
    }

    /**
     * Validate defectDescription không được trùng trong cùng file
     */
    private void validateDefectDescriptionDuplicates(List<DefectImportDto> parsedRows, List<ImportErrorItem> errors) {
        Map<String, Integer> defectDescriptionCount = new HashMap<>();
        Map<String, Integer> firstRowOfDescription = new HashMap<>();

        for (DefectImportDto row : parsedRows) {
            String defectDescription = normalize(row.getDefectDescription());
            if (defectDescription == null) {
                continue;
            }

            defectDescriptionCount.put(defectDescription, defectDescriptionCount.getOrDefault(defectDescription, 0) + 1);
            if (!firstRowOfDescription.containsKey(defectDescription)) {
                firstRowOfDescription.put(defectDescription, row.getExcelRowNumber());
            }
        }

        for (Map.Entry<String, Integer> entry : defectDescriptionCount.entrySet()) {
            if (entry.getValue() > 1) {
                String defectDescription = entry.getKey();
                Integer firstRow = firstRowOfDescription.get(defectDescription);

                for (DefectImportDto row : parsedRows) {
                    if (defectDescription.equals(normalize(row.getDefectDescription()))) {
                        errors.add(buildRowError(
                                row.getExcelRowNumber(),
                                "defectDescription",
                                defectDescription,
                                "defectDescription is duplicated with row " + firstRow
                        ));
                    }
                }
            }
        }
    }

    /**
     * Normalize string: trim, blank -> null
     */
    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    /**
     * Check blank
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Build ImportErrorItem
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
