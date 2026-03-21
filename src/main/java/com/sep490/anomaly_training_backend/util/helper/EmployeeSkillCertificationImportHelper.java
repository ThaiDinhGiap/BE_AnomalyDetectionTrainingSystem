package com.sep490.anomaly_training_backend.util.helper;

import com.sep490.anomaly_training_backend.dto.EmployeeSkillCertificationImportDto;
import com.sep490.anomaly_training_backend.dto.EmployeeSkillCertificationImportRowData;
import com.sep490.anomaly_training_backend.dto.ImportSkillMatrixResult;
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
 * Parse Team Code & Group Code từ file header
 * Parse data rows từ row 5+
 * ĐỒNG THỜI build map hierarchy: SectionCode → ProductLineCode → Set<ProcessName>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmployeeSkillCertificationImportHelper {

    // Header row indices (0-based)
    private static final int HEADER_TEAM_CODE_ROW = 0;    // Row 1
    private static final int HEADER_GROUP_CODE_ROW = 1;   // Row 2

    // Header column indices (0-based)
    private static final int HEADER_TEAM_CODE_COL = 1;    // Column B
    private static final int HEADER_GROUP_CODE_COL = 1;   // Column B

    // Data column indices (0-based)
    private static final int COL_SECTION_CODE = 0;
    private static final int COL_PRODUCT_LINE_CODE = 1;
    private static final int COL_PROCESS_NAME = 2;
    private static final int COL_CERTIFIED_QUANTITY = 3;
    private static final int COL_EMPLOYEE = 4;
    private static final int COL_CERTIFICATION_DATE = 5;
    private static final int COL_LAST_ACTION_DATE = 6;

    /**
     * Parse Excel file:
     * 1. Extract Team Code từ Row 1, Column B
     * 2. Extract Group Code từ Row 2, Column B
     * 3. Parse data rows từ row 5+
     * 4. Build hierarchy map
     */
    public ImportSkillMatrixResult parseExcelRowsWithHierarchy(
            Sheet sheet,
            List<ImportErrorItem> errors) {

        // Step 1: Extract Team Code & Group Code từ header
        String teamCode = extractTeamCode(sheet);
        String groupCode = extractGroupCode(sheet);

        if (teamCode == null || teamCode.isBlank()) {
            errors.add(buildRowError(1, "teamCode", null, "Team code is required (Row 1, Column B)"));
        }
        if (groupCode == null || groupCode.isBlank()) {
            errors.add(buildRowError(2, "groupCode", null, "Group code is required (Row 2, Column B)"));
        }

        List<EmployeeSkillCertificationImportDto> results = new ArrayList<>();
        Map<String, Map<String, Set<String>>> hierarchyMap = new HashMap<>();

        // Step 2: Parse raw data rows
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

        // Step 3: Apply carry-forward logic, build DTOs, AND build hierarchy map
        String currentSectionCode = null;
        String currentProductLineCode = null;
        String currentProcessName = null;

        for (EmployeeSkillCertificationImportRowData rawRow : rawRows) {
            // Update carry-forward values
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

            // Build resolved DTO với Team Code & Group Code từ header
            EmployeeSkillCertificationImportDto resolvedDto = EmployeeSkillCertificationImportDto.builder()
                    .excelRowNumber(rawRow.getExcelRowNumber())
                    .teamCode(teamCode)
                    .groupCode(groupCode)
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

            // BUILD HIERARCHY MAP
            addToHierarchyMap(hierarchyMap, currentSectionCode, currentProductLineCode, currentProcessName);
        }

        return new ImportSkillMatrixResult(teamCode, groupCode, results, hierarchyMap);
    }

    /**
     * Extract Team Code từ Row 1 (index 0), Column B (index 1)
     */
    private String extractTeamCode(Sheet sheet) {
        Row row = sheet.getRow(HEADER_TEAM_CODE_ROW);
        if (row == null) {
            return null;
        }
        Cell cell = row.getCell(HEADER_TEAM_CODE_COL);
        return getOptionalStringCellValue(cell);
    }

    /**
     * Extract Group Code từ Row 2 (index 1), Column B (index 1)
     */
    private String extractGroupCode(Sheet sheet) {
        Row row = sheet.getRow(HEADER_GROUP_CODE_ROW);
        if (row == null) {
            return null;
        }
        Cell cell = row.getCell(HEADER_GROUP_CODE_COL);
        return getOptionalStringCellValue(cell);
    }

    /**
     * Parse một raw row từ Excel (starting from row 5)
     */
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

    /**
     * Thêm entry vào hierarchy map
     */
    private void addToHierarchyMap(
            Map<String, Map<String, Set<String>>> hierarchyMap,
            String sectionCode,
            String productLineCode,
            String processName) {

        if (sectionCode == null || productLineCode == null || processName == null) {
            return;
        }

        hierarchyMap.putIfAbsent(sectionCode, new HashMap<>());
        Map<String, Set<String>> productLineMap = hierarchyMap.get(sectionCode);
        productLineMap.putIfAbsent(productLineCode, new HashSet<>());
        productLineMap.get(productLineCode).add(processName);
    }

    /**
     * Get value từ merged cell
     */
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

    /**
     * Check row trống
     */
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
     * Normalize string
     */
    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Parse integer từ string
     */
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

    /**
     * Parse LocalDate từ string (format: dd/MM/yyyy)
     */
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

    /**
     * Build error item
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