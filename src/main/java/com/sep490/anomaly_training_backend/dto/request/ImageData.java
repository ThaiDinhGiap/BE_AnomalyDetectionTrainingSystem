package com.sep490.anomaly_training_backend.dto.request;

import lombok.*;

/**
 * DTO to represent image data read from Excel
 * Contains the raw image bytes/stream along with metadata
 * 
 * Note: This is a transient object used only during import processing.
 * The actual persistence of image to storage (MinIO/DB) will be handled separately.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageData {
    
    /**
     * Image binary data
     */
    private byte[] imageBytes;
    
    /**
     * Image file name (extracted from embedded image metadata, may be null)
     */
    private String imageName;
    
    /**
     * Image MIME type (e.g., "image/png", "image/jpeg", etc.)
     * Will be determined from image bytes or embedded metadata
     */
    private String imageMimeType;
    
    /**
     * Image width in pixels (if available from metadata)
     */
    private Integer imageWidth;
    
    /**
     * Image height in pixels (if available from metadata)
     */
    private Integer imageHeight;
    
    /**
     * Excel cell reference where this image was found (e.g., "H3")
     */
    private String excelCellReference;
    
    /**
     * Row number from Excel (1-based)
     */
    private Integer excelRowNumber;
    
    /**
     * Flag indicating whether the image data was successfully extracted
     */
    private Boolean isSuccessfullyExtracted;
    
    /**
     * Error message if image extraction failed
     */
    private String extractionErrorMessage;
}

