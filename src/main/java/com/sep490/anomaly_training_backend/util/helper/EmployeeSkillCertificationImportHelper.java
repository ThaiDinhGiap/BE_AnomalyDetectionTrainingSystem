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
 * Helper to parse EmployeeSkillCertification from Excel import.
 *
 * NEW HEADER FORMAT (rows 1-2):
 *   Row 1: A="Team", B="T1 - Team 1",       D="Group",   E="SBU1 - Sleeve Backup 1", F="Manager",    G="8888 - Nguyễn Văn Huy"
 *   Row 2: A="Line", B="BU1 - Dây chuyền…",  C="Section", D="PRO7 - Section PRO7",    E="Supervisor", F="6666 - Mai Lan"
 *
 * NEW DATA COLUMNS (row 5+, 0-indexed):
 *   0=Pro(Section), 1=Line, 2=Mã công đoạn(processCode), 3=Công đoạn(processName),
 *   4=Loại công đoạn, 5=Số lượng NTT, 6=Người thao tác, 7=Ngày đạt CN, 8=Ngày TT gần nhất
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmployeeSkillCertificationImportHelper {

    // ============================================================
    // HEADER POSITIONS (0-based row/col)
    // ============================================================
    // Row 1 (index 0): Team@B, Group@D, Manager@F
    private static final int HEADER_ROW_1 = 0;
    private static final int HEADER_TEAM_VALUE_COL = 1;   // B
    private static final int HEADER_GROUP_VALUE_COL = 3;   // D (was at B)
    private static final int HEADER_MANAGER_VALUE_COL = 5; // F (was at E)

    // Row 2 (index 1): Line@B, Section@D, Supervisor@F
    private static final int HEADER_ROW_2 = 1;
    private static final int HEADER_LINE_VALUE_COL = 1;       // B
    private static final int HEADER_SECTION_VALUE_COL = 3;    // D
    private static final int HEADER_SUPERVISOR_VALUE_COL = 5; // F (was at E)

    // ============================================================
    // DATA COLUMN INDICES (0-based) — Column C "Mã công đoạn" inserted
    // ============================================================
    private static final int COL_SECTION_CODE = 0;      // A  - Pro (Section code)
    private static final int COL_PRODUCT_LINE_CODE = 1;  // B  - Line
    private static final int COL_PROCESS_CODE = 2;       // C  - Mã công đoạn (NEW)
    private static final int COL_PROCESS_NAME = 3;       // D  - Công đoạn (was C)
    // Column E = Loại công đoạn (not parsed)
    private static final int COL_CERTIFIED_QUANTITY = 5; // F  - Số lượng NTT (was E)
    private static final int COL_EMPLOYEE = 6;           // G  - Người thao tác (was F)
    private static final int COL_CERTIFICATION_DATE = 7; // H  - Ngày đạt CN (was G)
    private static final int COL_LAST_ACTION_DATE = 8;   // I  - Ngày TT gần nhất (was H)

    /**
     * Parse Excel file:
     * 1. Extract header info (Team, Group, Section, Line, Manager, Supervisor) — all as code-name
     * 2. Parse data rows from row 5+
     * 3. Build hierarchy map
     */
    public ImportSkillMatrixResult parseExcelRowsWithHierarchy(
            Sheet sheet,
            List<ImportErrorItem> errors) {

        // Step 1: Extract header values (all as "code - name")
        String[] teamParts = extractCodeAndName(sheet, HEADER_ROW_1, HEADER_TEAM_VALUE_COL);
        String[] groupParts = extractCodeAndName(sheet, HEADER_ROW_1, HEADER_GROUP_VALUE_COL);
        String[] managerParts = extractCodeAndName(sheet, HEADER_ROW_1, HEADER_MANAGER_VALUE_COL);

        String[] lineParts = extractCodeAndName(sheet, HEADER_ROW_2, HEADER_LINE_VALUE_COL);
        String[] sectionParts = extractCodeAndName(sheet, HEADER_ROW_2, HEADER_SECTION_VALUE_COL);
        String[] supervisorParts = extractCodeAndName(sheet, HEADER_ROW_2, HEADER_SUPERVISOR_VALUE_COL);

        String teamCode = teamParts[0];
        String teamName = teamParts[1];
        String groupCode = groupParts[0];
        String groupName = groupParts[1];
        String managerCode = managerParts[0];
        String managerName = managerParts[1];

        String lineCode = lineParts[0];
        String lineName = lineParts[1];
        String sectionCode = sectionParts[0];
        String sectionName = sectionParts[1];
        String supervisorCode = supervisorParts[0];
        String supervisorName = supervisorParts[1];

        // Validate required header fields
        if (teamCode == null || teamCode.isBlank()) {
            errors.add(buildRowError(1, "teamCode", null, "Team is required (Row 1, Column B). Format: code - name"));
        }
        if (groupCode == null || groupCode.isBlank()) {
            errors.add(buildRowError(1, "groupCode", null, "Group is required (Row 1, Column D). Format: code - name"));
        }
        if (managerCode == null || managerCode.isBlank()) {
            errors.add(buildRowError(1, "managerCode", null, "Manager is required (Row 1, Column F). Format: employeeCode - fullName"));
        }
        if (lineCode == null || lineCode.isBlank()) {
            errors.add(buildRowError(2, "lineCode", null, "Line is required (Row 2, Column B). Format: code - name"));
        }
        if (sectionCode == null || sectionCode.isBlank()) {
            errors.add(buildRowError(2, "sectionCode", null, "Section is required (Row 2, Column D). Format: code - name"));
        }
        if (supervisorCode == null || supervisorCode.isBlank()) {
            errors.add(buildRowError(2, "supervisorCode", null, "Supervisor is required (Row 2, Column F). Format: employeeCode - fullName"));
        }

        List<EmployeeSkillCertificationImportDto> results = new ArrayList<>();
        Map<String, Map<String, Set<String>>> hierarchyMap = new HashMap<>();

        // Step 2: Parse raw data rows (from row 5, index 4)
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
        String currentProcessCode = null;
        String currentProcessName = null;

        for (EmployeeSkillCertificationImportRowData rawRow : rawRows) {
            // Update carry-forward for Section (Pro column)
            if (rawRow.getSectionCodeColumn() != null && !rawRow.getSectionCodeColumn().trim().isEmpty()) {
                currentSectionCode = rawRow.getSectionCodeColumn().trim();
            } else {
                String mergedCode = getMergedCellValue(sheet, rawRow.getExcelRowNumber() - 1, COL_SECTION_CODE);
                if (mergedCode != null && !mergedCode.isEmpty()) {
                    currentSectionCode = mergedCode;
                }
            }

            // Update carry-forward for ProductLine (Line column)
            if (rawRow.getProductLineCodeColumn() != null && !rawRow.getProductLineCodeColumn().trim().isEmpty()) {
                currentProductLineCode = rawRow.getProductLineCodeColumn().trim();
            } else {
                String mergedCode = getMergedCellValue(sheet, rawRow.getExcelRowNumber() - 1, COL_PRODUCT_LINE_CODE);
                if (mergedCode != null && !mergedCode.isEmpty()) {
                    currentProductLineCode = mergedCode;
                }
            }

            // Update carry-forward for Process Code (NEW column C)
            if (rawRow.getProcessCodeColumn() != null && !rawRow.getProcessCodeColumn().trim().isEmpty()) {
                currentProcessCode = rawRow.getProcessCodeColumn().trim();
            } else {
                String mergedCode = getMergedCellValue(sheet, rawRow.getExcelRowNumber() - 1, COL_PROCESS_CODE);
                if (mergedCode != null && !mergedCode.isEmpty()) {
                    currentProcessCode = mergedCode;
                }
            }

            // Update carry-forward for Process Name (column D, was C)
            if (rawRow.getProcessNameColumn() != null && !rawRow.getProcessNameColumn().trim().isEmpty()) {
                currentProcessName = rawRow.getProcessNameColumn().trim();
            } else {
                String mergedProcess = getMergedCellValue(sheet, rawRow.getExcelRowNumber() - 1, COL_PROCESS_NAME);
                if (mergedProcess != null && !mergedProcess.isEmpty()) {
                    currentProcessName = mergedProcess;
                }
            }

            // Build resolved DTO
            EmployeeSkillCertificationImportDto resolvedDto = EmployeeSkillCertificationImportDto.builder()
                    .excelRowNumber(rawRow.getExcelRowNumber())
                    .teamCode(teamCode)
                    .groupCode(groupCode)
                    .sectionCode(currentSectionCode)
                    .productLineCode(currentProductLineCode)
                    .processCode(currentProcessCode)
                    .processName(currentProcessName)
                    .certifiedQuantity(parseInteger(rawRow.getCertifiedQuantityColumn()))
                    .employeeId(rawRow.getEmployeeId())
                    .employeeFullName(rawRow.getEmployeeFullName())
                    .certificationDate(rawRow.getCertificationDateColumn())
                    .lastActionDate(rawRow.getLastActionDateColumn())
                    .build();

            results.add(resolvedDto);

            // BUILD HIERARCHY MAP (using processCode now)
            addToHierarchyMap(hierarchyMap, currentSectionCode, currentProductLineCode, currentProcessCode);
        }

        return ImportSkillMatrixResult.builder()
                .teamCode(teamCode)
                .teamName(teamName)
                .groupCode(groupCode)
                .groupName(groupName)
                .sectionCode(sectionCode)
                .sectionName(sectionName)
                .lineCode(lineCode)
                .lineName(lineName)
                .managerCode(managerCode)
                .managerName(managerName)
                .supervisorCode(supervisorCode)
                .supervisorName(supervisorName)
                .parsedRows(results)
                .hierarchyMap(hierarchyMap)
                .build();
    }

    /**
     * Extract code and name from a cell with format "CODE - Name".
     * Returns String[2]: [code, name]. Both may be null if cell is empty.
     */
    private String[] extractCodeAndName(Sheet sheet, int rowIndex, int colIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) return new String[]{null, null};
        Cell cell = row.getCell(colIndex);
        String raw = getOptionalStringCellValue(cell);
        if (raw == null || raw.isBlank()) return new String[]{null, null};
        String[] parts = raw.split("-", 2);
        if (parts.length == 2) {
            return new String[]{parts[0].trim(), parts[1].trim()};
        }
        // No separator — treat entire value as code, name = code
        return new String[]{raw.trim(), raw.trim()};
    }

    /**
     * Parse a raw row from Excel (starting from row 5)
     */
    private EmployeeSkillCertificationImportRowData parseRawRow(Row row, int excelRowNumber, Sheet sheet) {
        String sectionCodeColumn = getOptionalStringCellValue(row.getCell(COL_SECTION_CODE));
        String productLineCodeColumn = getOptionalStringCellValue(row.getCell(COL_PRODUCT_LINE_CODE));
        String processCodeColumn = getOptionalStringCellValue(row.getCell(COL_PROCESS_CODE));
        String processNameColumn = getOptionalStringCellValue(row.getCell(COL_PROCESS_NAME));
        String certifiedQuantityColumn = getOptionalStringCellValue(row.getCell(COL_CERTIFIED_QUANTITY));
        String employeeColumn = getOptionalStringCellValue(row.getCell(COL_EMPLOYEE));
        LocalDate certificationDateColumn = getOptionalStringCellValue(row.getCell(COL_CERTIFICATION_DATE)) != null
                ? LocalDate.parse(getOptionalStringCellValue(row.getCell(COL_CERTIFICATION_DATE)))
                : null;
        LocalDate lastActionDateColumn = getOptionalStringCellValue(row.getCell(COL_LAST_ACTION_DATE)) != null
                ? LocalDate.parse(getOptionalStringCellValue(row.getCell(COL_LAST_ACTION_DATE)))
                : null;

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
                .processCodeColumn(processCodeColumn)
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
     * Add entry to hierarchy map (using processCode as key)
     */
    private void addToHierarchyMap(
            Map<String, Map<String, Set<String>>> hierarchyMap,
            String sectionCode,
            String productLineCode,
            String processCode) {

        if (sectionCode == null || productLineCode == null || processCode == null) {
            return;
        }

        hierarchyMap.putIfAbsent(sectionCode, new HashMap<>());
        Map<String, Set<String>> productLineMap = hierarchyMap.get(sectionCode);
        productLineMap.putIfAbsent(productLineCode, new HashSet<>());
        productLineMap.get(productLineCode).add(processCode);
    }

    /**
     * Get value from merged cell
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
     * Check if row is empty
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
     * Get optional string value from cell
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
     * Parse integer from string
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
     * Parse LocalDate from string (format: dd/MM/yyyy)
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