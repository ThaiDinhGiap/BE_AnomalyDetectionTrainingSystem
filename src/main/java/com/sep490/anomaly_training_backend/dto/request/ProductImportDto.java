package com.sep490.anomaly_training_backend.dto.request;

import lombok.*;

/**
 * DTO for parsing Product from Excel import
 * Represents a single parsed row from Product import
 * 
 * Excel structure:
 * Col 1 (A): No - skip
 * Col 2 (B): Mã sản phẩm (productCode)
 * Col 3 (C): Tên sản phẩm (productName)
 * Col 4 (D): Hình ảnh mô tả (image)
 * Col 5 (E): Mô tả sản phẩm (description)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImportDto {
    // Resolved values after parsing
    private String productCode;           // From col B - unique key
    private String productName;           // From col C
    private String description;           // From col E
    
    private ImageData imageData;          // From col D
    
    private Integer excelRowNumber;       // For error reporting
}

