package com.sep490.anomaly_training_backend.dto.request;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectImportDto {
    private String defectCode;
    private String defectDescription;
    private LocalDate detectedDate;
    private Boolean isEscaped;
    private String note;
    private String originCause;
    private String outflowCause;
    private String causePoint;
    private String processCode;
    private Integer excelRowNumber;  // NEW! Track which row this came from
}
