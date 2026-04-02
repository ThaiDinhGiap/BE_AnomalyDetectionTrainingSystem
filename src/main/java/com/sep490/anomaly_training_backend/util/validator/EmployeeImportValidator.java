package com.sep490.anomaly_training_backend.util.validator;

import com.sep490.anomaly_training_backend.dto.request.EmployeeImportDto;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Validator for Employee import data
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmployeeImportValidator {

    /**
     * Mapping from Excel display name → DB role_code
     * Based on V6 sample data:
     * "Admin"            → ROLE_ADMIN
     * "Manager"          → ROLE_MANAGER
     * "Supervisor"       → ROLE_SUPERVISOR
     * "Final Inspection" → ROLE_FINAL_INSPECTION
     * "Team Lead"        → ROLE_TEAM_LEADER
     */
    private static final Map<String, String> ROLE_DISPLAY_TO_CODE;

    static {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("Admin", "ROLE_ADMIN");
        map.put("Manager", "ROLE_MANAGER");
        map.put("Supervisor", "ROLE_SUPERVISOR");
        map.put("Final Inspection", "ROLE_FINAL_INSPECTION");
        map.put("Team Lead", "ROLE_TEAM_LEADER");
        ROLE_DISPLAY_TO_CODE = Collections.unmodifiableMap(map);
    }

    /**
     * Map role display name (from Excel) to role_code (in DB)
     *
     * @param displayName the role display name from Excel
     * @return the corresponding role_code, or null if not found
     */
    public static String mapRoleDisplayToCode(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return null;
        }
        // Case-insensitive lookup
        for (Map.Entry<String, String> entry : ROLE_DISPLAY_TO_CODE.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(displayName.trim())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Validate file data BEFORE database lookup
     * - Check required fields
     * - Check data format
     * - Check for duplicates in file
     * - Check role value validity
     */
    public void validateFileData(List<EmployeeImportDto> rows, List<ImportErrorItem> errors) {
        if (rows == null || rows.isEmpty()) {
            errors.add(ImportErrorItem.builder()
                    .field("SYSTEM")
                    .message("File contains no data rows")
                    .build());
            return;
        }

        // Track for duplicate detection
        Set<String> seenEmployeeCodes = new HashSet<>();
        Map<String, Integer> employeeCodeRowMap = new HashMap<>(); // For duplicate error reporting

        Set<String> seenEmails = new HashSet<>();
        Map<String, Integer> emailRowMap = new HashMap<>(); // For duplicate email error reporting

        for (EmployeeImportDto dto : rows) {
            int rowNum = dto.getExcelRowNumber();

            // 1. Check employeeCode is not null/empty
            if (dto.getEmployeeCode() == null || dto.getEmployeeCode().trim().isEmpty()) {
                errors.add(buildRowError(rowNum, "employeeCode", dto.getEmployeeCode(),
                        "Employee code is required"));
                continue; // Skip other checks if employeeCode is missing
            }

            // 2. Check fullName is not null/empty
            if (dto.getFullName() == null || dto.getFullName().trim().isEmpty()) {
                errors.add(buildRowError(rowNum, "fullName", dto.getFullName(),
                        "Full name is required"));
            }

            // 3. Check for duplicate employeeCode in file
            if (seenEmployeeCodes.contains(dto.getEmployeeCode())) {
                Integer firstRow = employeeCodeRowMap.get(dto.getEmployeeCode());
                errors.add(buildRowError(rowNum, "employeeCode", dto.getEmployeeCode(),
                        "Duplicate employee code in file (first occurrence at row " + firstRow + ")"));
            } else {
                seenEmployeeCodes.add(dto.getEmployeeCode());
                employeeCodeRowMap.put(dto.getEmployeeCode(), rowNum);
            }

            // 4. If role is not blank → validate role + email required
            boolean hasRole = dto.getRole() != null && !dto.getRole().trim().isEmpty();
            if (hasRole && !dto.getRole().equalsIgnoreCase("Team Lead")) {
                // 4a. Validate role value
                String roleCode = mapRoleDisplayToCode(dto.getRole());
                if (roleCode == null) {
                    errors.add(buildRowError(rowNum, "role", dto.getRole(),
                            "Invalid role value. Allowed: Admin, Manager, Supervisor, Final Inspection, Team Lead"));
                }

                // 4b. Email is required when role is present (because User will be created)
                if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
                    errors.add(buildRowError(rowNum, "email", dto.getEmail(),
                            "Email is required when role is specified"));
                } else {
                    // 4c. Check for duplicate email in file
                    String normalizedEmail = dto.getEmail().trim().toLowerCase();
                    if (seenEmails.contains(normalizedEmail)) {
                        Integer firstRow = emailRowMap.get(normalizedEmail);
                        errors.add(buildRowError(rowNum, "email", dto.getEmail(),
                                "Duplicate email in file (first occurrence at row " + firstRow + ")"));
                    } else {
                        seenEmails.add(normalizedEmail);
                        emailRowMap.put(normalizedEmail, rowNum);
                    }
                }
            }
        }

        log.info("File validation completed. Total rows: {}, Errors: {}", rows.size(), errors.size());
    }

    /**
     * Build error item for a row
     */
    private ImportErrorItem buildRowError(Integer rowNumber, String field, String value, String message) {
        return ImportErrorItem.builder()
                .rowNumber(rowNumber)
                .field(field)
                .value(value)
                .message(message)
                .build();
    }
}
