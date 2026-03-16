package com.sep490.anomaly_training_backend.dto.request;

import lombok.*;

/**
 * DTO for parsing ProductLine+Process data from Excel import
 * Each row represents one Process, but ProductLine info comes from merged cells
 *
 * Excel structure:
 * Col 1 (A): No - skip
 * Col 2 (B): Tên dây chuyền (productLineName) - MERGED CELLS
 * Col 3 (C): Mã dây chuyền (productLineCode) - MERGED CELLS
 * Col 4 (D): Tên công đoạn (processName) - unique per row
 * Col 5 (E): Mã công đoạn (processCode) - unique per row (and per ProductLine)
 * Col 6 (F): Mô tả công đoạn (processDescription)
 * Col 7 (G): Phân loại công đoạn (processClassification)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductLineImportDto {
    // ProductLine info (from merged cells - same for multiple rows)
    private String productLineName;         // Col B - MERGED
    private String productLineCode;         // Col C - MERGED
    
    // Process info (unique per row)
    private String processName;             // Col D
    private String processCode;             // Col E
    private String processDescription;      // Col F
    private String processClassification;   // Col G (enum: C3, C4, C5, etc)
    
    private Integer excelRowNumber;         // For error reporting
}
