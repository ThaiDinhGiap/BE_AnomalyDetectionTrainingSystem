package com.sep490.anomaly_training_backend.dto.request;

import lombok.*;

/**
 * Raw row data from Excel before carry-forward logic applied
 * Used for intermediate parsing before resolving carry-forward values
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingSampleImportRowData {
    // Raw column values (may be null/blank)
    private String processColumn;
    private String categoryColumn;
    private String defectColumn;
    private String trainingDescriptionColumn;
    private String trainingSampleCodeColumn;
    private String trainingCodeColumn;
    private String noteColumn;
    
    // Image data from Excel (from column 8/I)
    private ImageData imageData;

    // Excel position
    private Integer excelRowNumber;

    // Flag to indicate if row is completely empty
    private Boolean isEmpty;

    // Flag to indicate if this row has only category/process info (header row of category block)
    private Boolean isHeaderRow;
}

