package com.sep490.anomaly_training_backend.util.helper;

import com.sep490.anomaly_training_backend.dto.request.DefectImportDto;
import com.sep490.anomaly_training_backend.dto.request.ImageData;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefectImportHelper {
    // 0-based column index
    private static final int COL_DEFECT_CODE = 1;
    private static final int COL_PROCESS = 2;
    private static final int COL_DEFECT_DESCRIPTION = 3;
    private static final int COL_IMAGE = 4;
    private static final int COL_DETECTED_DATE = 5;
    private static final int COL_ORIGIN_CAUSE = 6;
    private static final int COL_OUTFLOW_CAUSE = 7;
    private static final int COL_ORIGIN_MEASURE = 8;
    private static final int COL_OUTFLOW_MEASURE = 9;
    private static final int COL_PRODUCT = 10;
    private static final int COL_CUSTOMER = 11;
    private static final int COL_QUANTITY = 12;
    private static final int COL_CONCLUSION = 13;
    private static final int COL_ESCAPED = 14;
    private static final int COL_CUSTOMER_CLAIM = 15;
    private static final int COL_STARTLED_CLAIM = 16;

    /**
     * Parse toàn bộ data row từ row 3 trở đi.
     */
    public List<DefectImportDto> parseExcelRows(
            Sheet sheet,
            List<ImportErrorItem> errors
    ) {
        List<DefectImportDto> results = new ArrayList<>();

        for (int i = 2; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }
            try {
                results.add(parseRow(row, i + 1));
            } catch (Exception e) {
                errors.add(buildRowError(i + 1, "ROW", null, "Error parsing row: " + e.getMessage()));
            }
        }
        return results;
    }

    private DefectImportDto parseRow(Row row, int excelRowNumber) throws BadRequestException {
        String defectCode = getOptionalStringCellValue(row.getCell(COL_DEFECT_CODE));
        String processCode = getOptionalStringCellValue(row.getCell(COL_PROCESS));
        String defectDescription = getOptionalStringCellValue(row.getCell(COL_DEFECT_DESCRIPTION));
        LocalDate detectedDate = getLocalDateCellValue(row.getCell(COL_DETECTED_DATE), "Ngày phát sinh", excelRowNumber);
        String originCause = getOptionalStringCellValue(row.getCell(COL_ORIGIN_CAUSE));
        String outflowCause = getOptionalStringCellValue(row.getCell(COL_OUTFLOW_CAUSE));
        String outflowMeasures = getOptionalStringCellValue(row.getCell(COL_OUTFLOW_MEASURE));
        String originMeasures = getOptionalStringCellValue(row.getCell(COL_ORIGIN_MEASURE));
        String productCode = getOptionalStringCellValue(row.getCell(COL_PRODUCT));
        String customer = getOptionalStringCellValue(row.getCell(COL_CUSTOMER));
        Integer quantity = getOptionalIntegerCellValue(row.getCell(COL_QUANTITY));
        String conclusion = getOptionalStringCellValue(row.getCell(COL_CONCLUSION));

        Boolean isEscaped = getOptionalBooleanCellValue(row.getCell(COL_ESCAPED),"PPGH", excelRowNumber);
        Boolean customerClaim = getOptionalBooleanCellValue(row.getCell(COL_CUSTOMER_CLAIM), "customerClaim", excelRowNumber);
        Boolean startledClaim = getOptionalBooleanCellValue(row.getCell(COL_STARTLED_CLAIM), "startledClaim", excelRowNumber);

        ImageData imageData = extractImageFromRow(row, excelRowNumber);

        return DefectImportDto.builder()
                .excelRowNumber(excelRowNumber)
                .defectCode(defectCode)
                .processCode(processCode)
                .defectDescription(defectDescription)
                .imageData(imageData)
                .detectedDate(detectedDate)
                .originCause(originCause)
                .outflowCause(outflowCause)
                .outflowMeasures(outflowMeasures)
                .originMeasures(originMeasures)
                .isEscape(isEscaped)
                .customerClaim(customerClaim)
                .startledClaim(startledClaim)
                .productCode(productCode)
                .customer(customer)
                .quantity(quantity)
                .conclusion(conclusion)
                .build();
    }

    // ===================== IMAGE =====================

    private ImageData extractImageFromRow(Row row, int excelRowNumber) {
        try {
            Sheet sheet = row.getSheet();
            if (!(sheet instanceof XSSFSheet xssfSheet)) {
                log.warn("Sheet is not XSSF, skip image extraction for row {}", excelRowNumber);
                return null;
            }

            XSSFDrawing drawing = xssfSheet.getDrawingPatriarch();
            if (drawing == null) {
                return null;
            }

            for (XSSFShape shape : drawing.getShapes()) {
                if (!(shape instanceof XSSFPicture picture)) {
                    continue;
                }

                if (isPictureInRowAndColumn(picture, row.getRowNum(), COL_IMAGE)) {
                    return extractPictureData(picture, excelRowNumber);
                }
            }

            return null;
        } catch (Exception e) {
            log.error("Error extracting image from row {}: {}", excelRowNumber, e.getMessage(), e);
            return null;
        }
    }

    private boolean isPictureInRowAndColumn(XSSFPicture picture, int rowIndex, int colIndex) {
        try {
            XSSFClientAnchor anchor = picture.getClientAnchor();
            if (anchor == null) {
                return false;
            }

            int startRow = anchor.getRow1();
            int endRow = anchor.getRow2();
            int startCol = anchor.getCol1();
            int endCol = anchor.getCol2();

            boolean rowOverlap = startRow <= rowIndex && endRow >= rowIndex;
            boolean colOverlap = startCol <= colIndex && endCol >= colIndex;

            return rowOverlap && colOverlap;
        } catch (Exception e) {
            log.error("Error checking picture position: {}", e.getMessage(), e);
            return false;
        }
    }

    private ImageData extractPictureData(XSSFPicture picture, int excelRowNumber) {
        try {
            XSSFClientAnchor anchor = picture.getClientAnchor();
            if (anchor == null) {
                log.warn("Picture has no anchor information");
                return null;
            }

            byte[] imageBytes = picture.getPictureData().getData();
            if (imageBytes == null || imageBytes.length == 0) {
                log.warn("Picture contains no data");
                return null;
            }

            String mimeType = picture.getPictureData().getMimeType();
            String cellReference = getCellReference(anchor.getRow1(), anchor.getCol1());

            log.debug("Extracted image data: size={} bytes, mimeType={}, cellRef={}", imageBytes.length, mimeType, cellReference);

            return ImageData.builder()
                    .imageBytes(imageBytes)
                    .imageMimeType(mimeType)
                    .excelCellReference(cellReference)
                    .excelRowNumber(excelRowNumber)
                    .isSuccessfullyExtracted(true)
                    .build();

        } catch (Exception e) {
            log.error("Error extracting picture data: {}", e.getMessage(), e);
            return ImageData.builder()
                    .excelRowNumber(excelRowNumber)
                    .isSuccessfullyExtracted(false)
                    .extractionErrorMessage(e.getMessage())
                    .build();
        }
    }

    // ===================== CELL UTILS =====================

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int c = 0; c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellDisplayValue(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    private String getOptionalStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

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
            case BLANK -> null;
            default -> normalize(cell.toString());
        };
    }

    private LocalDate getLocalDateCellValue(Cell cell, String fieldName, int excelRowNumber) throws BadRequestException {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }

            if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                if (value.isBlank()) {
                    return null;
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return LocalDate.parse(value, formatter);
            }

            throw new BadRequestException("Row " + excelRowNumber + ": " + fieldName + " has invalid format");
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException(
                    "Row " + excelRowNumber + ": " + fieldName + " has invalid value: " + getCellDisplayValue(cell)
            );
        }
    }

    private String getCellDisplayValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
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
            case FORMULA -> cell.toString();
            case BLANK -> "";
            default -> cell.toString();
        };
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Integer getOptionalIntegerCellValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                double value = cell.getNumericCellValue();
                if (value == (long) value) {
                    return (int) value;
                }
                return (int) Math.round(value);
            }

            if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                if (value.isEmpty()) {
                    return null;
                }
                return Integer.parseInt(value);
            }

            return null;
        } catch (Exception e) {
            log.warn("Error parsing integer value from cell: {}", getCellDisplayValue(cell), e);
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

    private String getCellReference(int rowIndex, int colIndex) {
        StringBuilder cellRef = new StringBuilder();

        int col = colIndex + 1;
        while (col > 0) {
            col--;
            cellRef.insert(0, (char) ('A' + (col % 26)));
            col /= 26;
        }

        cellRef.append(rowIndex + 1);
        return cellRef.toString();
    }
    private Boolean getOptionalBooleanCellValue(Cell cell, String fieldName, int excelRowNumber) throws BadRequestException {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return false;
        }

        if (cell.getCellType() != CellType.STRING) {
            throw new BadRequestException(
                    "Row " + excelRowNumber + ": " + fieldName + " chỉ được nhập 'V' hoặc để trống"
            );
        }

        String value = cell.getStringCellValue();
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        if ("V".equals(value.trim())) {
            return true;
        }

        throw new BadRequestException(
                "Row " + excelRowNumber + ": " + fieldName + " chỉ được nhập 'V' hoặc để trống"
        );
    }
}
