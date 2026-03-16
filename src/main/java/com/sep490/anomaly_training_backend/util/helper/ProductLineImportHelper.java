package com.sep490.anomaly_training_backend.util.helper;

import com.sep490.anomaly_training_backend.dto.request.ProductLineImportDto;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import com.sep490.anomaly_training_backend.util.ExcelImageExtractorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper để parse ProductLine+Process từ Excel import
 * Xử lý merge cells - ProductLine columns bị merge theo nhiều dòng
 *
 * Excel structure:
 * Row 1: Header
 * Row 2+: Data
 *
 * Cột (0-based):
 * 0: No - bỏ qua
 * 1: Tên dây chuyền (productLineName) - MERGED CELLS
 * 2: Mã dây chuyền (productLineCode) - MERGED CELLS
 * 3: Tên công đoạn (processName)
 * 4: Mã công đoạn (processCode)
 * 5: Mô tả công đoạn (processDescription)
 * 6: Phân loại công đoạn (processClassification)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductLineImportHelper {

    // 0-based column index
    private static final int COL_PRODUCT_LINE_NAME = 1;
    private static final int COL_PRODUCT_LINE_CODE = 2;
    private static final int COL_PROCESS_NAME = 3;
    private static final int COL_PROCESS_CODE = 4;
    private static final int COL_PROCESS_DESCRIPTION = 5;
    private static final int COL_PROCESS_CLASSIFICATION = 6;

    /**
     * Parse tất cả rows từ sheet, xử lý merge cells cho ProductLine columns
     */
    public List<ProductLineImportDto> parseExcelRows(
            Sheet sheet,
            List<ImportErrorItem> errors
    ) {
        List<ProductLineImportDto> results = new ArrayList<>();
        String currentProductLineName = null;   // Track ProductLine từ merged cell
        String currentProductLineCode = null;

        // Start từ row 2 (0-based index = 1, Excel row = 2)
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            try {
                ProductLineImportDto dto = parseRow(row, i + 1, sheet);

                // Xử lý merge cells cho ProductLine columns
                // Nếu productLineName trống → lookup merge cell
                if (dto.getProductLineName() != null && !dto.getProductLineName().trim().isEmpty()) {
                    currentProductLineName = dto.getProductLineName();
                } else {
                    String mergedName = getMergedCellValue(sheet, i, COL_PRODUCT_LINE_NAME);
                    if (mergedName != null && !mergedName.isEmpty()) {
                        dto.setProductLineName(mergedName);
                        currentProductLineName = mergedName;
                    } else {
                        dto.setProductLineName(currentProductLineName);
                    }
                }

                // Tương tự cho productLineCode
                if (dto.getProductLineCode() != null && !dto.getProductLineCode().trim().isEmpty()) {
                    currentProductLineCode = dto.getProductLineCode();
                } else {
                    String mergedCode = getMergedCellValue(sheet, i, COL_PRODUCT_LINE_CODE);
                    if (mergedCode != null && !mergedCode.isEmpty()) {
                        dto.setProductLineCode(mergedCode);
                        currentProductLineCode = mergedCode;
                    } else {
                        dto.setProductLineCode(currentProductLineCode);
                    }
                }

                results.add(dto);

            } catch (Exception e) {
                log.error("Error parsing row {}: {}", i + 1, e.getMessage());
                errors.add(buildRowError(i + 1, "ROW", null, "Error parsing row: " + e.getMessage()));
            }
        }

        return results;
    }

    /**
     * Parse một row thành ProductLineImportDto
     */
    private ProductLineImportDto parseRow(Row row, int excelRowNumber, Sheet sheet) {
        String productLineName = getOptionalStringCellValue(row.getCell(COL_PRODUCT_LINE_NAME));
        String productLineCode = getOptionalStringCellValue(row.getCell(COL_PRODUCT_LINE_CODE));
        String processName = getOptionalStringCellValue(row.getCell(COL_PROCESS_NAME));
        String processCode = getOptionalStringCellValue(row.getCell(COL_PROCESS_CODE));
        String processDescription = getOptionalStringCellValue(row.getCell(COL_PROCESS_DESCRIPTION));
        String processClassification = getOptionalStringCellValue(row.getCell(COL_PROCESS_CLASSIFICATION));

        return ProductLineImportDto.builder()
                .excelRowNumber(excelRowNumber)
                .productLineName(productLineName)
                .productLineCode(productLineCode)
                .processName(processName)
                .processCode(processCode)
                .processDescription(processDescription)
                .processClassification(processClassification)
                .build();
    }

    /**
     * Get value từ merged cell
     */
    private String getMergedCellValue(Sheet sheet, int rowIndex, int colIndex) {
        if (!(sheet instanceof XSSFSheet xSheet)) {
            return null;
        }

        // Duyệt qua tất cả merged regions
        for (CellRangeAddress mergedRegion : xSheet.getMergedRegions()) {
            // Kiểm tra xem (rowIndex, colIndex) có nằm trong merged region này không
            if (mergedRegion.isInRange(rowIndex, colIndex)) {
                // Lấy ô đầu của merged region
                Row firstRow = sheet.getRow(mergedRegion.getFirstRow());
                if (firstRow != null) {
                    Cell cell = firstRow.getCell(mergedRegion.getFirstColumn());
                    if (cell != null) {
                        return getOptionalStringCellValue(cell);
                    }
                }
            }
        }

        return null;
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
}

