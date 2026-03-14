package com.sep490.anomaly_training_backend.util.helper;

import com.sep490.anomaly_training_backend.dto.request.ImageData;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleImportDto;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleImportRowData;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Helper class to parse TrainingSample Excel import
 * ONLY responsible for:
 * - Parse Excel rows into structured data
 * - Apply carry-forward logic (process, category, defect inheritance)
 * - Calculate order fields (processOrder, categoryOrder, contentOrder)
 *
 * Does NOT validate against database
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TrainingSampleImportHelper {

    // Excel column indices (0-based)
    private static final int COL_PROCESS = 1;
    private static final int COL_CATEGORY = 2;
    private static final int COL_DEFECT = 3;
    private static final int COL_TRAINING_DESCRIPTION = 4;
    private static final int COL_TRAINING_SAMPLE_CODE = 5;
    private static final int COL_PRODUCT_CODE = 6;
    private static final int COL_TRAINING_CODE = 7;
    private static final int COL_IMAGE = 8;           // NEW: Column for embedded image
    private static final int COL_NOTE = 9;            // UPDATED: Note moved to column 9

    /**
     * Parse Excel file starting from row 3
     * Apply carry-forward logic and calculate order fields
     */
    public List<TrainingSampleImportDto> parseExcelRowsWithCarryForward(
            Sheet sheet,
            List<ImportErrorItem> errors) {

        List<TrainingSampleImportRowData> rawRows = new ArrayList<>();

        // Step 1: Parse all raw rows
        for (int i = 2; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            if (isRowEmpty(row)) {
                continue;
            }

            try {
                TrainingSampleImportRowData rowData = parseRawRow(row, i + 1);
                rawRows.add(rowData);
            } catch (Exception e) {
                errors.add(buildRowError(i + 1, "ROW", null, "Error parsing row: " + e.getMessage()));
            }
        }

        // Step 2: Apply carry-forward logic and build resolved DTOs
        List<TrainingSampleImportDto> resolvedRows = new ArrayList<>();
        String currentProcess = null;
        String currentCategory = null;
        String currentDefect = null;

        int processOrderCounter = 1;
        String lastProcessCode = null;
        Map<String, Integer> categoryOrderMap = new HashMap<>();
        Map<String, Integer> contentOrderMap = new HashMap<>();

        for (TrainingSampleImportRowData rawRow : rawRows) {
            // Update carry-forward values
            if (isNotBlank(rawRow.getProcessColumn())) {
                currentProcess = rawRow.getProcessColumn().trim();
                lastProcessCode = currentProcess;
                processOrderCounter++;
                categoryOrderMap.clear();
                contentOrderMap.clear();
            }

            if (isNotBlank(rawRow.getCategoryColumn())) {
                currentCategory = rawRow.getCategoryColumn().trim();
                String categoryKey = lastProcessCode + "|" + currentCategory;
                categoryOrderMap.put(categoryKey, categoryOrderMap.getOrDefault(categoryKey, 0) + 1);
                contentOrderMap.put(categoryKey, 0);
            }

            if (isNotBlank(rawRow.getDefectColumn())) {
                currentDefect = rawRow.getDefectColumn().trim();
            }

            // Determine if this is header row or content row
            String trainingDesc = rawRow.getTrainingDescriptionColumn();
            boolean isHeaderRow = isBlank(trainingDesc);

            // Skip pure header rows (don't add to resolved list)
            if (isHeaderRow) {
                continue;
            }

            // Build resolved DTO
            String categoryKey = lastProcessCode + "|" + currentCategory;
            Integer categoryOrder = categoryOrderMap.getOrDefault(categoryKey, 1);
            Integer contentOrder = contentOrderMap.getOrDefault(categoryKey, 0) + 1;
            contentOrderMap.put(categoryKey, contentOrder);

            TrainingSampleImportDto resolvedDto = TrainingSampleImportDto.builder()
                .processCode(currentProcess)
                .categoryName(currentCategory)
                .defectCode(currentDefect)
                .trainingDescription(trainingDesc.trim())
                .trainingSampleCode(normalize(rawRow.getTrainingSampleCodeColumn()))
                .productCode(normalize(rawRow.getProductCodeColumn()))
                .trainingCode(normalize(rawRow.getTrainingCodeColumn()))
                .imageData(rawRow.getImageData())  // Carry forward image data
                .processOrder(lastProcessCode != null ? processOrderCounter : 1)
                .categoryOrder(categoryOrder)
                .contentOrder(contentOrder)
                .excelRowNumber(rawRow.getExcelRowNumber())
                .note(normalize(rawRow.getNoteColumn()))
                .isHeaderRow(false)
                .build();

            resolvedRows.add(resolvedDto);
        }

        return resolvedRows;
    }

    /**
     * Parse raw Excel row into TrainingSampleImportRowData
     */
    private TrainingSampleImportRowData parseRawRow(Row row, int excelRowNumber) {
        String processColumn = getOptionalStringCellValue(row.getCell(COL_PROCESS));
        String categoryColumn = getOptionalStringCellValue(row.getCell(COL_CATEGORY));
        String defectColumn = getOptionalStringCellValue(row.getCell(COL_DEFECT));
        String trainingDescriptionColumn = getOptionalStringCellValue(row.getCell(COL_TRAINING_DESCRIPTION));
        String trainingSampleCodeColumn = getOptionalStringCellValue(row.getCell(COL_TRAINING_SAMPLE_CODE));
        String productCodeColumn = getOptionalStringCellValue(row.getCell(COL_PRODUCT_CODE));
        String trainingCodeColumn = getOptionalStringCellValue(row.getCell(COL_TRAINING_CODE));
        String noteColumn = getOptionalStringCellValue(row.getCell(COL_NOTE));
        
        // Extract image data from column 8 (COL_IMAGE)
        ImageData imageData = extractImageFromRow(row, excelRowNumber);

        return TrainingSampleImportRowData.builder()
            .processColumn(processColumn)
            .categoryColumn(categoryColumn)
            .defectColumn(defectColumn)
            .trainingDescriptionColumn(trainingDescriptionColumn)
            .trainingSampleCodeColumn(trainingSampleCodeColumn)
            .productCodeColumn(productCodeColumn)
            .trainingCodeColumn(trainingCodeColumn)
            .noteColumn(noteColumn)
            .imageData(imageData)
            .excelRowNumber(excelRowNumber)
            .isEmpty(false)
            .isHeaderRow(isBlank(trainingDescriptionColumn))
            .build();
    }

    // ============= Cell Reading Utilities =============

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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    private ImportErrorItem buildRowError(Integer rowNumber, String field, String value, String message) {
        return ImportErrorItem.builder()
            .rowNumber(rowNumber)
            .field(field)
            .value(value)
            .message(message)
            .build();
    }

    // ============= Image Reading Methods =============

    /**
     * Extract image data from the specified row
     * Searches for images that overlap with the image column (COL_IMAGE) area
     *
     * @param row the row to search for images
     * @param excelRowNumber the 1-based row number from Excel
     * @return ImageData with image information, or null if no image found
     */
    private ImageData extractImageFromRow(Row row, int excelRowNumber) {
        try {
            Sheet sheet = row.getSheet();
            if (!(sheet instanceof XSSFSheet xssfSheet)) {
                log.warn("Sheet is not XSSF format, cannot extract images from row {}", excelRowNumber);
                return null;
            }

            // Get all drawing shapes/images from the sheet
            XSSFDrawing drawing = xssfSheet.getDrawingPatriarch();
            if (drawing == null) {
                return null;
            }

            // Search for images that intersect with the image column area
            ImageData imageData = null;

            for (XSSFShape shape : drawing.getShapes()) {
                if (shape instanceof XSSFPicture picture) {
                    // Check if this picture overlaps with the current row and image column
                    if (isPictureInRowAndColumn(picture, row.getRowNum(), COL_IMAGE)) {
                        try {
                            imageData = extractPictureData(picture, excelRowNumber);
                            if (imageData != null) {
                                log.info("Successfully extracted image from row {} ({})", excelRowNumber, imageData.getExcelCellReference());
                                break; // Take the first matching image for this row
                            }
                        } catch (Exception e) {
                            log.error("Error extracting image data from picture in row {}: {}", excelRowNumber, e.getMessage(), e);
                        }
                    }
                }
            }

            return imageData;
        } catch (Exception e) {
            log.error("Error extracting image from row {}: {}", excelRowNumber, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Check if a picture is positioned in the specified row and column area
     *
     * @param picture the XSSFPicture to check
     * @param rowIndex the 0-based row index
     * @param colIndex the 0-based column index
     * @return true if picture overlaps with the row/column area
     */
    private boolean isPictureInRowAndColumn(XSSFPicture picture, int rowIndex, int colIndex) {
        try {
            XSSFClientAnchor anchor = picture.getClientAnchor();
            if (anchor == null) {
                return false;
            }

            // Check if picture starts in or overlaps with the target row/column
            int pictureStartRow = anchor.getRow1();
            int pictureStartCol = anchor.getCol1();
            int pictureEndRow = anchor.getRow2();
            int pictureEndCol = anchor.getCol2();

            // Check if picture overlaps with the specified row and column
            // Picture overlaps if:
            // - It starts in the row or before it, and ends in the row or after it
            // - It starts in the column or before it, and ends in the column or after it
            boolean rowOverlap = (pictureStartRow <= rowIndex) && (pictureEndRow >= rowIndex);
            boolean colOverlap = (pictureStartCol <= colIndex) && (pictureEndCol >= colIndex);

            return rowOverlap && colOverlap;
        } catch (Exception e) {
            log.error("Error checking picture position: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract binary data and metadata from an XSSFPicture
     *
     * @param picture the XSSFPicture to extract data from
     * @param excelRowNumber the 1-based row number from Excel
     * @return ImageData with image bytes and metadata
     */
    private ImageData extractPictureData(XSSFPicture picture, int excelRowNumber) {
        try {
            XSSFClientAnchor anchor = picture.getClientAnchor();
            if (anchor == null) {
                log.warn("Picture has no anchor information");
                return null;
            }

            // Get the picture bytes
            byte[] imageBytes = picture.getPictureData().getData();
            if (imageBytes == null || imageBytes.length == 0) {
                log.warn("Picture contains no data");
                return null;
            }

            // Determine MIME type from picture type
            String mimeType = picture.getPictureData().getMimeType();

            // Cell reference where picture starts
            String cellReference = getCellReference(anchor.getRow1(), anchor.getCol1());

            // TODO: Extract image dimensions if available from metadata
            // TODO: Extract image name if available from picture properties
            // For now, dimensions and name are null - can be added in future

            ImageData imageData = ImageData.builder()
                .imageBytes(imageBytes)
                .imageMimeType(mimeType)
                .excelCellReference(cellReference)
                .excelRowNumber(excelRowNumber)
                .isSuccessfullyExtracted(true)
                .build();

            log.debug("Extracted image data: size={} bytes, mimeType={}, cellRef={}", imageBytes.length, mimeType, cellReference);
            return imageData;
        } catch (Exception e) {
            log.error("Error extracting picture data: {}", e.getMessage(), e);
            return ImageData.builder()
                .isSuccessfullyExtracted(false)
                .extractionErrorMessage(e.getMessage())
                .excelRowNumber(excelRowNumber)
                .build();
        }
    }



    /**
     * Convert row and column indices to Excel cell reference (e.g., "A1", "H3")
     *
     * @param rowIndex 0-based row index
     * @param colIndex 0-based column index
     * @return Excel cell reference string
     */
    private String getCellReference(int rowIndex, int colIndex) {
        StringBuilder cellRef = new StringBuilder();

        // Convert column index to letters
        int col = colIndex + 1;
        while (col > 0) {
            col--;
            cellRef.insert(0, (char) ('A' + (col % 26)));
            col /= 26;
        }

        // Add row number (1-based)
        cellRef.append(rowIndex + 1);

        return cellRef.toString();
    }
}
