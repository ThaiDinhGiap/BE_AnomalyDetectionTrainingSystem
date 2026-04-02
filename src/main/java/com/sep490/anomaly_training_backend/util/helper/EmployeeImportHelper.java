package com.sep490.anomaly_training_backend.util.helper;

import com.sep490.anomaly_training_backend.dto.request.EmployeeImportDto;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper để parse Employee từ Excel import
 * <p>
 * Excel structure:
 * Row 1: Header
 * Row 2+: Data
 * <p>
 * Cột (0-based):
 * 0: No - bỏ qua (STT)
 * 1: Mã nhân viên (employeeCode)
 * 2: Họ tên (fullName)
 * 3: Email (email)
 * 4: Chức vụ (role) - optional
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmployeeImportHelper {

    // 0-based column index
    private static final int COL_FULL_NAME = 1;
    private static final int COL_EMPLOYEE_CODE = 2;
    private static final int COL_EMAIL = 3;
    private static final int COL_ROLE = 4;

    /**
     * Parse tất cả Employee rows từ sheet
     */
    public List<EmployeeImportDto> parseExcelRows(
            Sheet sheet,
            List<ImportErrorItem> errors
    ) {
        List<EmployeeImportDto> results = new ArrayList<>();

        // Start từ row 2 (0-based index = 1, Excel row = 2)
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            try {
                EmployeeImportDto dto = parseRow(row, i + 1);
                results.add(dto);

            } catch (Exception e) {
                log.error("Error parsing row {}: {}", i + 1, e.getMessage());
                errors.add(buildRowError(i + 1, "ROW", null, "Error parsing row: " + e.getMessage()));
            }
        }

        return results;
    }

    /**
     * Parse một row thành EmployeeImportDto
     */
    private EmployeeImportDto parseRow(Row row, int excelRowNumber) {
        String employeeCode = getOptionalStringCellValue(row.getCell(COL_EMPLOYEE_CODE));
        String fullName = getOptionalStringCellValue(row.getCell(COL_FULL_NAME));
        String email = getOptionalStringCellValue(row.getCell(COL_EMAIL));
        String role = getOptionalStringCellValue(row.getCell(COL_ROLE));

        return EmployeeImportDto.builder()
                .excelRowNumber(excelRowNumber)
                .employeeCode(employeeCode)
                .fullName(fullName)
                .email(email)
                .role(role)
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
}
