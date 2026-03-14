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
    private String defectCodeColumn;
    private String defectDescriptionColumn;
    private LocalDate detectedDateColumn;
    private String noteColumn;
    private String originCauseColumn;
    private String outflowCauseColumn;
    private String causePointColumn;

    private ImageData imageData;
    private String originMeasuresColumn;
    private String outflowMeasuresColumn;
    private Boolean isEscapeColumn;
    private Boolean customerClaimColumn;
    private Boolean startledClaimColumn;

    private String processCodeColumn;
    private Integer excelRowNumber;  // NEW! Track which row this came from
}
