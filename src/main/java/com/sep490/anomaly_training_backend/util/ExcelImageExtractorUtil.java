package com.sep490.anomaly_training_backend.util;

import com.sep490.anomaly_training_backend.dto.request.ImageData;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;

/**
 * Utility class để extract hình ảnh từ Excel file
 * Cung cấp cơ chế chung cho tất cả các helper import (TrainingSample, Product, Defect)
 *
 * Quy ước:
 * - Mỗi file Excel có 1 cột "Hình ảnh mô tả" tại vị trí chỉ định
 * - Mỗi row chỉ có duy nhất 1 ảnh
 * - Ảnh được nhúng vào Excel và định vị theo row/column
 */
@Slf4j
public class ExcelImageExtractorUtil {

    /**
     * Extract ảnh từ một row cụ thể trong Excel
     *
     * @param sheet Sheet từ workbook
     * @param rowIndex 0-based row index (dùng để check vị trí ảnh)
     * @param colIndex 0-based column index (cột "Hình ảnh mô tả")
     * @param excelRowNumber 1-based row number từ Excel (dùng để report lỗi)
     * @return ImageData nếu tìm thấy ảnh, null nếu không có ảnh
     */
    public static ImageData extractImageFromRow(Sheet sheet, int rowIndex, int colIndex, int excelRowNumber) {
        try {
            if (!(sheet instanceof XSSFSheet xssfSheet)) {
                log.debug("Sheet is not XSSF format, cannot extract images from row {}", excelRowNumber);
                return null;
            }

            // Get all drawing shapes/images from the sheet
            XSSFDrawing drawing = xssfSheet.getDrawingPatriarch();
            if (drawing == null) {
                log.debug("No drawing found in sheet for row {}", excelRowNumber);
                return null;
            }

            // Search for images that intersect with the specified row and column
            for (XSSFShape shape : drawing.getShapes()) {
                if (shape instanceof XSSFPicture picture) {
                    // Check if this picture overlaps with the current row and image column
                    if (isPictureInRowAndColumn(picture, rowIndex, colIndex)) {
                        try {
                            ImageData imageData = extractPictureData(picture, excelRowNumber);
                            if (imageData != null) {
                                log.info("Successfully extracted image from row {} ({})", excelRowNumber, imageData.getExcelCellReference());
                                return imageData; // Return first matching image for this row
                            }
                        } catch (Exception e) {
                            log.error("Error extracting image data from picture in row {}: {}", excelRowNumber, e.getMessage(), e);
                            // Continue searching for other images
                        }
                    }
                }
            }

            log.debug("No image found for row {} column {}", excelRowNumber, colIndex);
            return null;

        } catch (Exception e) {
            log.error("Error extracting image from row {} column {}: {}", excelRowNumber, colIndex, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Check if a picture is positioned in the specified row and column area
     *
     * @param picture the XSSFPicture to check
     * @param rowIndex 0-based row index
     * @param colIndex 0-based column index
     * @return true if picture overlaps with the row/column area
     */
    private static boolean isPictureInRowAndColumn(XSSFPicture picture, int rowIndex, int colIndex) {
        try {
            XSSFClientAnchor anchor = picture.getClientAnchor();
            if (anchor == null) {
                return false;
            }

            // Picture position
            int pictureStartRow = anchor.getRow1();
            int pictureStartCol = anchor.getCol1();
            int pictureEndRow = anchor.getRow2();
            int pictureEndCol = anchor.getCol2();

            // Check if picture overlaps with the specified row and column
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
     * @param excelRowNumber 1-based row number from Excel
     * @return ImageData with image bytes and metadata
     */
    private static ImageData extractPictureData(XSSFPicture picture, int excelRowNumber) {
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

            // Build ImageData
            ImageData imageData = ImageData.builder()
                    .imageBytes(imageBytes)
                    .imageMimeType(mimeType)
                    .excelCellReference(cellReference)
                    .excelRowNumber(excelRowNumber)
                    .isSuccessfullyExtracted(true)
                    .build();

            log.debug("Extracted image data: size={} bytes, mimeType={}, cellRef={}, row={}",
                    imageBytes.length, mimeType, cellReference, excelRowNumber);
            return imageData;

        } catch (Exception e) {
            log.error("Error extracting picture data from row {}: {}", excelRowNumber, e.getMessage(), e);
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
    private static String getCellReference(int rowIndex, int colIndex) {
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


