package com.sep490.anomaly_training_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSkillCertificationImportRowData {
    private Integer excelRowNumber;

    private String sectionCodeColumn;
    private String productLineCodeColumn;
    private String processNameColumn;
    private String certifiedQuantityColumn;
    private String employeeColumn;
    private String certificationDateColumn;
    private String lastActionDateColumn;

    private String employeeId;
    private String employeeFullName;
}