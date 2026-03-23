package com.sep490.anomaly_training_backend.dto.response.dashboard;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MngTrainingProgressPoint {
    String lineName;
    double previousPassRate;   // pass rate of previous month (%)
    double currentPassRate;    // pass rate of selected month (%)
}
