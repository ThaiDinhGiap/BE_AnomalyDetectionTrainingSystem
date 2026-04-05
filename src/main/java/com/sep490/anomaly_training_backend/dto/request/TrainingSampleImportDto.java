package com.sep490.anomaly_training_backend.dto.request;

import lombok.*;

/**
 * DTO for parsing TrainingSample from Excel import
 * Represents a single parsed row after carry-forward logic applied
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingSampleImportDto {
    // Resolved values after carry-forward logic
    private String processCode;
    private String categoryName;
    private String defectCode;

    // Required content fields
    private String trainingDescription;
    private String trainingCode;

    // Optional fields
    private String trainingSampleCode;

    // Image data from Excel
    private ImageData imageData;

    // Metadata for ordering
    private Integer processOrder;
    private Integer categoryOrder;
    private Integer contentOrder;
    private Integer excelRowNumber;
    private String note;

    // Flag to indicate this is a header/category definition row (no content)
    private Boolean isHeaderRow;
}

