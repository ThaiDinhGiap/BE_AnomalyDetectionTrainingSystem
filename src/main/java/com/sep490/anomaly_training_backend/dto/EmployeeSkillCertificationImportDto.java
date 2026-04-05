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
public class EmployeeSkillCertificationImportDto {
    private Integer excelRowNumber;

    private String teamCode;
    private String groupCode;

    private String sectionCode;
    private String productLineCode;
    private String processCode;
    private String processName;

    private Integer certifiedQuantity;
    private String employeeId;
    private String employeeFullName;
    private LocalDate certificationDate;
    private LocalDate lastActionDate;
}