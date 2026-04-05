package com.sep490.anomaly_training_backend.dto.request;

import lombok.*;

/**
 * DTO for parsing Employee from Excel import
 * Represents a single parsed row from Employee import
 *
 * Excel structure:
 * Col 1 (A): No - skip (STT)
 * Col 2 (B): Mã nhân viên (employeeCode)
 * Col 3 (C): Họ tên (fullName)
 * Col 4 (D): Email (email)
 * Col 5 (E): Chức vụ (role) - optional
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeImportDto {
    // Resolved values after parsing
    private String employeeCode;      // From col B - unique key
    private String fullName;          // From col C
    private String email;             // From col D - required only when role is not blank
    private String role;              // From col E - optional, display name from Excel

    private Integer excelRowNumber;   // For error reporting
}
