package com.sep490.anomaly_training_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSkillCertificationImportRowData {
    private Integer excelRowNumber;

    private String sectionCodeColumn;
    private String productLineCodeColumn;
    private String processCodeColumn;
    private String processNameColumn;
    private String certifiedQuantityColumn;
    private String employeeColumn;
    private LocalDate certificationDateColumn;
    private LocalDate lastActionDateColumn;

    private String employeeId;
    private String employeeFullName;
}