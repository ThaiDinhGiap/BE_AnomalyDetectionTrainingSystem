package com.sep490.anomaly_training_backend.util.helper;

import com.sep490.anomaly_training_backend.dto.request.ImageData;
import com.sep490.anomaly_training_backend.dto.request.ProductImportDto;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import com.sep490.anomaly_training_backend.util.ExcelImageExtractorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper để parse Product từ Excel import
 *
 * Excel structure:
 * Row 1: Header
 * Row 2+: Data
 *
 * Cột (0-based):
 * 0: No - bỏ qua
 * 1: Mã sản phẩm (productCode)
 * 2: Tên sản phẩm (productName)
 * 3: Hình ảnh mô tả (image)
 * 4: Mô tả sản phẩm (description)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductImportHelper {

    // 0-based column index
    private static final int COL_PRODUCT_CODE = 1;
    private static final int COL_PRODUCT_NAME = 2;
    private static final int COL_IMAGE = 3;
    private static final int COL_DESCRIPTION = 4;

    /**
     * Parse tất cả Product rows từ sheet
     */
    public List<ProductImportDto> parseExcelRows(
            Sheet sheet,
            List<ImportErrorItem> errors
    ) {
        List<ProductImportDto> results = new ArrayList<>();

        // Start từ row 2 (0-based index = 1, Excel row = 2)
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            try {
                ProductImportDto dto = parseRow(row, i + 1);
                results.add(dto);

            } catch (Exception e) {
                log.error("Error parsing row {}: {}", i + 1, e.getMessage());
                errors.add(buildRowError(i + 1, "ROW", null, "Error parsing row: " + e.getMessage()));
            }
        }

        return results;
    }

    /**
     * Parse một row thành ProductImportDto
     */
    private ProductImportDto parseRow(Row row, int excelRowNumber) {
        String productCode = getOptionalStringCellValue(row.getCell(COL_PRODUCT_CODE));
        String productName = getOptionalStringCellValue(row.getCell(COL_PRODUCT_NAME));
        String description = getOptionalStringCellValue(row.getCell(COL_DESCRIPTION));

        ImageData imageData = extractImageFromRow(row, excelRowNumber);

        return ProductImportDto.builder()
                .excelRowNumber(excelRowNumber)
                .productCode(productCode)
                .productName(productName)
                .description(description)
                .imageData(imageData)
                .build();
    }


    /**
     * Check row trống
     */
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getOptionalStringCellValue(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Lấy optional string value từ cell
     */
    private String getOptionalStringCellValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        try {
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
                default -> normalize(cell.toString());
            };
        } catch (Exception e) {
            log.warn("Error reading cell value: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Normalize string - xóa khoảng trắng, return null nếu trống
     */
    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Build error item cho row
     */
    private ImportErrorItem buildRowError(Integer rowNumber, String field, String value, String message) {
        return ImportErrorItem.builder()
                .rowNumber(rowNumber)
                .field(field)
                .value(value)
                .message(message)
                .build();
    }

    /**
     * Extract image data from the specified row
     * Delegates to ExcelImageExtractorUtil for common image extraction logic
     *
     * @param row            the row to search for images
     * @param excelRowNumber the 1-based row number from Excel
     * @return ImageData with image information, or null if no image found
     */
    private ImageData extractImageFromRow(Row row, int excelRowNumber) {
        if (row == null) {
            return null;
        }

        Sheet sheet = row.getSheet();
        int rowIndex = row.getRowNum(); // 0-based row index

        // Delegate to utility class for image extraction
        return ExcelImageExtractorUtil.extractImageFromRow(sheet, rowIndex, COL_IMAGE, excelRowNumber);
    }
}
