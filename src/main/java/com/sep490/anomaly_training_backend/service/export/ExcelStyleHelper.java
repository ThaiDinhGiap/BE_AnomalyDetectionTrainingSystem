package com.sep490.anomaly_training_backend.service.export;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Shared styling utilities for Excel export.
 * Created once per Workbook to avoid style duplication.
 */
public class ExcelStyleHelper {

    private final Workbook workbook;
    private final CellStyle headerStyle;
    private final CellStyle dataStyle;
    private final CellStyle dateStyle;
    private final CellStyle sectionHeaderStyle;
    private final CellStyle numberStyle;

    public ExcelStyleHelper(Workbook workbook) {
        this.workbook = workbook;
        this.headerStyle = createHeaderStyle();
        this.dataStyle = createDataStyle();
        this.dateStyle = createDateStyle();
        this.sectionHeaderStyle = createSectionHeaderStyle();
        this.numberStyle = createNumberStyle();
    }

    public CellStyle getHeaderStyle() { return headerStyle; }
    public CellStyle getDataStyle() { return dataStyle; }
    public CellStyle getDateStyle() { return dateStyle; }
    public CellStyle getSectionHeaderStyle() { return sectionHeaderStyle; }
    public CellStyle getNumberStyle() { return numberStyle; }

    /**
     * Write a header row with given column names.
     */
    public void writeHeaderRow(Sheet sheet, int rowNum, String... headers) {
        Row row = sheet.createRow(rowNum);
        row.setHeightInPoints(25);
        for (int i = 0; i < headers.length; i++) {
            var cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * Write a data cell at the given row/col with automatic type detection.
     */
    public void writeCell(Row row, int col, Object value) {
        var cell = row.createCell(col);
        if (value == null) {
            cell.setCellValue("");
            cell.setCellStyle(dataStyle);
        } else if (value instanceof Number num) {
            cell.setCellValue(num.doubleValue());
            cell.setCellStyle(numberStyle);
        } else if (value instanceof LocalDate date) {
            cell.setCellValue(date);
            cell.setCellStyle(dateStyle);
        } else if (value instanceof LocalDateTime dateTime) {
            cell.setCellValue(dateTime);
            cell.setCellStyle(dateStyle);
        } else if (value instanceof Boolean bool) {
            cell.setCellValue(bool ? "Có" : "Không");
            cell.setCellStyle(dataStyle);
        } else {
            cell.setCellValue(value.toString());
            cell.setCellStyle(dataStyle);
        }
    }

    /**
     * Write a section header spanning across columns (e.g., "THÔNG TIN CHUNG").
     */
    public void writeSectionHeader(Sheet sheet, int rowNum, String title) {
        Row row = sheet.createRow(rowNum);
        row.setHeightInPoints(22);
        var cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(sectionHeaderStyle);
    }

    /**
     * Write a key-value info row (e.g., "Mã phiếu: | DP-001").
     */
    public void writeInfoRow(Sheet sheet, int rowNum, String label, Object value) {
        Row row = sheet.createRow(rowNum);
        var labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(sectionHeaderStyle);
        writeCell(row, 1, value);
    }

    /**
     * Auto-size all columns in sheet up to given column count.
     */
    public void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            // Set minimum width of 12 characters
            if (sheet.getColumnWidth(i) < 3200) {
                sheet.setColumnWidth(i, 3200);
            }
            // Cap max width at 60 characters
            if (sheet.getColumnWidth(i) > 15360) {
                sheet.setColumnWidth(i, 15360);
            }
        }
    }

    // ── Private style builders ──────────────────────────────────────────────

    private CellStyle createHeaderStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        applyBorder(style);
        return style;
    }

    private CellStyle createDataStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        applyBorder(style);
        return style;
    }

    private CellStyle createDateStyle() {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("dd/MM/yyyy"));
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorder(style);
        return style;
    }

    private CellStyle createSectionHeaderStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createNumberStyle() {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.##"));
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorder(style);
        return style;
    }

    private void applyBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
