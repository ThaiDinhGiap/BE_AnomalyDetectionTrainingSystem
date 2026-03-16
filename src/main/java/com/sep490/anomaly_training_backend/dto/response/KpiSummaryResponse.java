package com.sep490.anomaly_training_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class KpiSummaryResponse {
    private long totalExecuted;
    private long totalPass;
    private long totalFail;
    private BigDecimal passRate;
}
