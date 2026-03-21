package com.sep490.anomaly_training_backend.util.helper;

import com.sep490.anomaly_training_backend.dto.EmployeeSkillCertificationImportDto;
import com.sep490.anomaly_training_backend.dto.ImportSkillMatrixResult;
import com.sep490.anomaly_training_backend.dto.request.EmployeeSkillCertificationImportRowData;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper để parse EmployeeSkillCertification từ Excel import
 * CHỈ parse raw data, không validate database
 * ĐỒNG THỜI build map hierarchy: SectionCode → ProductLineCode → Set<ProcessName>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmployeeSkillCertificationImportHelper {

    // 0-based column indices
    private static final int COL_SECTION_CODE = 0;
    private static final int COL_PRODUCT_LINE_CODE = 1;
    private static final int COL_PROCESS_NAME = 2;
    private static final int COL_CERTIFIED_QUANTITY = 3;
    private static final int COL_EMPLOYEE = 4;
    private static final int COL_CERTIFICATION_DATE = 5;
    private static final int COL_LAST_ACTION_DATE = 6;

    /**
     * Parse tất cả rows từ sheet, xử lý merged cells
     * Đồng thời build hierarchy map
     *
     * @param sheet  Excel sheet
     * @param errors List để collect parse errors
     * @return ImportSkillMatrixResult chứa cả parsed rows và hierarchy map
     */
    public ImportSkillMatrixResult parseExcelRowsWithHierarchy(
            Sheet sheet,
            List<ImportErrorItem> errors) {

        List<EmployeeSkillCertificationImportDto> results = new ArrayList<>();
        Map<String, Map<String, Set<String>>> hierarchyMap = new HashMap<>();

        // Step 1: Parse raw rows
        List<EmployeeSkillCertificationImportRowData> rawRows = new ArrayList<>();
        for (int i = 4; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            try {
                EmployeeSkillCertificationImportRowData rawData = parseRawRow(row, i + 1, sheet);
                rawRows.add(rawData);
            } catch (Exception e) {
                log.error("Error parsing row {}: {}", i + 1, e.getMessage());
                errors.add(buildRowError(i + 1, "ROW", null, "Error parsing row: " + e.getMessage()));
            }
        }

        // Step 2: Apply carry-forward logic, build DTOs, AND build hierarchy map
        String currentSectionCode = null;
        String currentProductLineCode = null;
        String currentProcessName = null;

        for (EmployeeSkillCertificationImportRowData rawRow : rawRows) {
            // Update carry-forward values - handle merged cells
            if (rawRow.getSectionCodeColumn() != null && !rawRow.getSectionCodeColumn().trim().isEmpty()) {
                currentSectionCode = rawRow.getSectionCodeColumn().trim();
            } else {
                String mergedCode = getMergedCellValue(sheet, rawRow.getExcelRowNumber() - 1, COL_SECTION_CODE);
                if (mergedCode != null && !mergedCode.isEmpty()) {
                    currentSectionCode = mergedCode;
                }
            }

            if (rawRow.getProductLineCodeColumn() != null && !rawRow.getProductLineCodeColumn().trim().isEmpty()) {
                currentProductLineCode = rawRow.getProductLineCodeColumn().trim();
            } else {
                String mergedCode = getMergedCellValue(sheet, rawRow.getExcelRowNumber() - 1, COL_PRODUCT_LINE_CODE);
                if (mergedCode != null && !mergedCode.isEmpty()) {
                    currentProductLineCode = mergedCode;
                }
            }

            if (rawRow.getProcessNameColumn() != null && !rawRow.getProcessNameColumn().trim().isEmpty()) {
                currentProcessName = rawRow.getProcessNameColumn().trim();
            } else {
                String mergedProcess = getMergedCellValue(sheet, rawRow.getExcelRowNumber() - 1, COL_PROCESS_NAME);
                if (mergedProcess != null && !mergedProcess.isEmpty()) {
                    currentProcessName = mergedProcess;
                }
            }

            // Build resolved DTO with carry-forward values
            EmployeeSkillCertificationImportDto resolvedDto = EmployeeSkillCertificationImportDto.builder()
                    .excelRowNumber(rawRow.getExcelRowNumber())
                    .sectionCode(currentSectionCode)
                    .productLineCode(currentProductLineCode)
                    .processName(currentProcessName)
                    .certifiedQuantity(parseInteger(rawRow.getCertifiedQuantityColumn()))
                    .employeeId(rawRow.getEmployeeId())
                    .employeeFullName(rawRow.getEmployeeFullName())
                    .certificationDate(rawRow.getCertificationDateColumn() != null
                            ? parseLocalDate(rawRow.getCertificationDateColumn())
                            : null)
                    .lastActionDate(rawRow.getLastActionDateColumn() != null
                            ? parseLocalDate(rawRow.getLastActionDateColumn())
                            : null)
                    .build();

            results.add(resolvedDto);

            // BUILD HIERARCHY MAP NGAY TẠI ĐÂY
            addToHierarchyMap(hierarchyMap, currentSectionCode, currentProductLineCode, currentProcessName);
        }

        return new ImportSkillMatrixResult(results, hierarchyMap);
    }

    /**
     * Thêm entry vào hierarchy map
     * Map<SectionCode, Map<ProductLineCode, Set<ProcessName>>>
     * <p>
     * Tự động deduplication vì sử dụng Set
     */
    private void addToHierarchyMap(
            Map<String, Map<String, Set<String>>> hierarchyMap,
            String sectionCode,
            String productLineCode,
            String processName) {

        if (sectionCode == null || productLineCode == null || processName == null) {
            return; // Skip nếu thiếu dữ liệu
        }

        // Level 1: Section
        hierarchyMap.putIfAbsent(sectionCode, new HashMap<>());

        // Level 2: ProductLine
        Map<String, Set<String>> productLineMap = hierarchyMap.get(sectionCode);
        productLineMap.putIfAbsent(productLineCode, new HashSet<>());

        // Level 3: Process (auto-dedup với Set)
        Set<String> processSet = productLineMap.get(productLineCode);
        processSet.add(processName);
    }

    // ====== CÁC HÀM HỖ TRỢ CŨ (giữ nguyên) ======

    private EmployeeSkillCertificationImportRowData parseRawRow(Row row, int excelRowNumber, Sheet sheet) {
        String sectionCodeColumn = getOptionalStringCellValue(row.getCell(COL_SECTION_CODE));
        String productLineCodeColumn = getOptionalStringCellValue(row.getCell(COL_PRODUCT_LINE_CODE));
        String processNameColumn = getOptionalStringCellValue(row.getCell(COL_PROCESS_NAME));
        String certifiedQuantityColumn = getOptionalStringCellValue(row.getCell(COL_CERTIFIED_QUANTITY));
        String employeeColumn = getOptionalStringCellValue(row.getCell(COL_EMPLOYEE));
        String certificationDateColumn = getOptionalStringCellValue(row.getCell(COL_CERTIFICATION_DATE));
        String lastActionDateColumn = getOptionalStringCellValue(row.getCell(COL_LAST_ACTION_DATE));

        String employeeId = null;
        String employeeFullName = null;
        if (employeeColumn != null && !employeeColumn.trim().isEmpty()) {
            String[] parts = employeeColumn.split("-", 2);
            if (parts.length == 2) {
                employeeId = parts[0].trim();
                employeeFullName = parts[1].trim();
            } else {
                employeeFullName = employeeColumn.trim();
            }
        }

        return EmployeeSkillCertificationImportRowData.builder()
                .excelRowNumber(excelRowNumber)
                .sectionCodeColumn(sectionCodeColumn)
                .productLineCodeColumn(productLineCodeColumn)
                .processNameColumn(processNameColumn)
                .certifiedQuantityColumn(certifiedQuantityColumn)
                .employeeColumn(employeeColumn)
                .certificationDateColumn(certificationDateColumn)
                .lastActionDateColumn(lastActionDateColumn)
                .employeeId(employeeId)
                .employeeFullName(employeeFullName)
                .build();
    }

    private String getMergedCellValue(Sheet sheet, int rowIndex, int colIndex) {
        if (!(sheet instanceof XSSFSheet xSheet)) {
            return null;
        }

        for (CellRangeAddress mergedRegion : xSheet.getMergedRegions()) {
            if (mergedRegion.isInRange(rowIndex, colIndex)) {
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

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int c = 0; c < row.getLastCellNum(); c++) {
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

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Cannot parse integer from: {}", value);
            return null;
        }
    }

    private LocalDate parseLocalDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(value.trim(), formatter);
        } catch (Exception e) {
            log.warn("Cannot parse date from: {}", value);
            return null;
        }
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