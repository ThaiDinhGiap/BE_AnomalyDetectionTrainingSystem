package com.sep490.anomaly_training_backend.util.validator;

import com.sep490.anomaly_training_backend.dto.request.ProductImportDto;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Validator for Product import data
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductImportValidator {

    /**
     * Validate file data BEFORE database lookup
     * - Check required fields
     * - Check data format
     * - Check for duplicates in file
     */
    public void validateFileData(List<ProductImportDto> rows, List<ImportErrorItem> errors) {
        if (rows == null || rows.isEmpty()) {
            errors.add(ImportErrorItem.builder()
                    .field("SYSTEM")
                    .message("File contains no data rows")
                    .build());
            return;
        }

        // Track for duplicate detection
        Set<String> seenProductCodes = new HashSet<>();
        Map<String, Integer> productCodeRowMap = new HashMap<>(); // For duplicate error reporting

        for (ProductImportDto dto : rows) {
            int rowNum = dto.getExcelRowNumber();

            // 1. Check productCode is not null/empty
            if (dto.getProductCode() == null || dto.getProductCode().trim().isEmpty()) {
                errors.add(buildRowError(rowNum, "productCode", dto.getProductCode(), 
                        "Product code is required"));
                continue; // Skip other checks if productCode is missing
            }

            // 2. Check productName is not null/empty
            if (dto.getProductName() == null || dto.getProductName().trim().isEmpty()) {
                errors.add(buildRowError(rowNum, "productName", dto.getProductName(), 
                        "Product name is required"));
            }

            // 3. Check for duplicate productCode in file
            if (seenProductCodes.contains(dto.getProductCode())) {
                // Find first occurrence
                Integer firstRow = productCodeRowMap.get(dto.getProductCode());
                errors.add(buildRowError(rowNum, "productCode", dto.getProductCode(), 
                        "Duplicate product code in file (first occurrence at row " + firstRow + ")"));
            } else {
                seenProductCodes.add(dto.getProductCode());
                productCodeRowMap.put(dto.getProductCode(), rowNum);
            }
        }

        log.info("File validation completed. Total rows: {}, Errors: {}", rows.size(), errors.size());
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

