package com.sep490.anomaly_training_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class ImportSkillMatrixResult {
    // Header: Team (code - name)
    private String teamCode;
    private String teamName;

    // Header: Group (code - name)
    private String groupCode;
    private String groupName;

    // Header: Section (code - name)
    private String sectionCode;
    private String sectionName;

    // Header: Line / ProductLine (code - name)
    private String lineCode;
    private String lineName;

    // Header: Manager (employeeCode - fullName)
    private String managerCode;
    private String managerName;

    // Header: Supervisor (employeeCode - fullName)
    private String supervisorCode;
    private String supervisorName;

    private List<EmployeeSkillCertificationImportDto> parsedRows;
    private Map<String, Map<String, Set<String>>> hierarchyMap;
}
