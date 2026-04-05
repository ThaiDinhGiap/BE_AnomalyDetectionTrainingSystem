package com.sep490.anomaly_training_backend.dto.request;

import lombok.*;

import java.time.LocalDate;

/**
 * DTO for parsing Defect from Excel import
 * Represents a single parsed row after carry-forward logic applied
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectImportDto {
    // Resolved values after carry-forward logic
    private String defectDescription;
    private LocalDate detectedDate;
    private String note;
    private String originCause;
    private String outflowCause;
    private String causePoint;

    private ImageData imageData;
    private String originMeasures;
    private String outflowMeasures;
    private Boolean isEscape;
    private Boolean customerClaim;
    private Boolean startledClaim;

    private String processCode;

    private String customer;
    private Integer quantity;
    private String conclusion;
    private String productCode;

    private Integer excelRowNumber;
}
