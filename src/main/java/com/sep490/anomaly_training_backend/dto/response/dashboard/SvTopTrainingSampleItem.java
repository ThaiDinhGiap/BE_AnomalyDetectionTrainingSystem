package com.sep490.anomaly_training_backend.dto.response.dashboard;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SvTopTrainingSampleItem {
    String name;      // process name (e.g. "HL Lắp ráp P01")
    int used;          // total evaluated count
    double failRate;   // fail percentage
}
