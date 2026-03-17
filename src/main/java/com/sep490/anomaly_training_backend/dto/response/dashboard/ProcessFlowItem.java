package com.sep490.anomaly_training_backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessFlowItem {
    private Long id;
    private String code;            // "OP-01"
    private String name;            // "Machining"
    private int classification;     // 1-4 (C1-C4)
}
