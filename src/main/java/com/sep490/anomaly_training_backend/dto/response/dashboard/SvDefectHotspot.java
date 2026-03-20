package com.sep490.anomaly_training_backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SvDefectHotspot {
    private int rank;
    private String title;      // "CĐ-05 (Lắp vỏ)"
    private String line;       // "Dây chuyền Sleeve 7"
    private int count;
    private boolean danger;    // true if count > threshold
}
