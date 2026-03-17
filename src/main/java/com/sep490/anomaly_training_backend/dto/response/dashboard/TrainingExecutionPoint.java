package com.sep490.anomaly_training_backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingExecutionPoint {
    private String date;    // "01/03"
    private int keHoach;    // cumulative planned
    private int thucTe;     // cumulative actual done
    private int biLo;       // cumulative missed
}
