package com.sep490.anomaly_training_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectImportRowData {
    private String defectCode;
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
    private Integer excelRowNumber;  // NEW! Track which row this came from
}
