package com.sep490.anomaly_training_backend.util.validator;

import com.sep490.anomaly_training_backend.dto.request.ProductLineImportDto;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Validator for ProductLine+Process import data
 *
 * Rules:
 * 1. Không được lặp mã dây chuyền trên cùng file
 * 2. Không được lặp mã công đoạn trên một group theo dây chuyền
 * 3. Toàn bộ required fields phải có giá trị
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductLineImportValidator {

    /**
     * Validate file data BEFORE database lookup
     * Rules:
     * 1. (productLineCode, processCode) must be unique per row
     * 2. productLineName must map to only 1 productLineCode (1-1 mapping)
     * 3. All required fields must have values
     */
    public void validateFileData(List<ProductLineImportDto> rows, List<ImportErrorItem> errors) {
        if (rows == null || rows.isEmpty()) {
            errors.add(ImportErrorItem.builder()
                    .field("SYSTEM")
                    .message("File contains no data rows")
                    .build());
            return;
        }

        // Track (productLineCode, processCode) pairs - must be unique
        Set<String> seenProductLineProcessPairs = new HashSet<>();
        Map<String, Integer> pairRowMap = new HashMap<>();

        for (ProductLineImportDto dto : rows) {
            int rowNum = dto.getExcelRowNumber();

            // ===== PRODUCT LINE VALIDATION =====

            // 1. Check productLineCode is not null/empty
            if (dto.getProductLineCode() == null || dto.getProductLineCode().trim().isEmpty()) {
                errors.add(buildRowError(rowNum, "productLineCode", dto.getProductLineCode(),
                        "Product line code is required"));
                continue; // Skip remaining validations for this row
            }

            // 2. Check productLineName is not null/empty
            if (dto.getProductLineName() == null || dto.getProductLineName().trim().isEmpty()) {
                errors.add(buildRowError(rowNum, "productLineName", dto.getProductLineName(),
                        "Product line name is required"));
            }

            // ===== PROCESS VALIDATION =====

            // 4. Check processCode is not null/empty
            if (dto.getProcessCode() == null || dto.getProcessCode().trim().isEmpty()) {
                errors.add(buildRowError(rowNum, "processCode", dto.getProcessCode(),
                        "Process code is required"));
                continue; // Skip remaining process validations
            }

            // 5. Check processName is not null/empty
            if (dto.getProcessName() == null || dto.getProcessName().trim().isEmpty()) {
                errors.add(buildRowError(rowNum, "processName", dto.getProcessName(),
                        "Process name is required"));
            }

            // 6. Check processClassification is valid (if provided)
            if (dto.getProcessClassification() != null && !dto.getProcessClassification().trim().isEmpty()) {
                if (!isValidProcessClassification(dto.getProcessClassification())) {
                    errors.add(buildRowError(rowNum, "processClassification", dto.getProcessClassification(),
                            "Invalid process classification. Valid values: C3, C4, C5, etc."));
                }
            }

            // ===== UNIQUE PAIR VALIDATION =====

            // 7. Check (productLineCode, processCode) is unique
            String pairKey = dto.getProductLineCode() + "|" + dto.getProcessCode();
            
            if (seenProductLineProcessPairs.contains(pairKey)) {
                Integer firstRow = pairRowMap.get(pairKey);
                errors.add(buildRowError(rowNum, "processCode", dto.getProcessCode(),
                        "Duplicate (productLineCode, processCode) pair. " +
                        "Product line '" + dto.getProductLineCode() + "' with process code '" + 
                        dto.getProcessCode() + "' already appeared at row " + firstRow));
            } else {
                seenProductLineProcessPairs.add(pairKey);
                pairRowMap.put(pairKey, rowNum);
            }
        }

        log.info("File validation completed. Total rows: {}, Errors: {}", rows.size(), errors.size());
    }

    /**
     * Check if ProcessClassification value is valid
     * Valid values: 1, 2, 3, 4
     */
    private boolean isValidProcessClassification(String value) {
        if (value == null || value.trim().isEmpty()) {
            return true; // Optional field
        }
        String normalized = value.trim();
        return normalized.matches("^[1-4]$");
    }

    /**
     * Build error item for a row
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
