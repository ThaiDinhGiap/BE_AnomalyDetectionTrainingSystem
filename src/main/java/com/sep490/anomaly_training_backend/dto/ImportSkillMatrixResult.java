package com.sep490.anomaly_training_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
public class ImportSkillMatrixResult {
    private String teamCode;
    private String groupCode;
    private String managerCode;
    private String supervisorCode;
    private List<EmployeeSkillCertificationImportDto> parsedRows;
    private Map<String, Map<String, Set<String>>> hierarchyMap;
}
