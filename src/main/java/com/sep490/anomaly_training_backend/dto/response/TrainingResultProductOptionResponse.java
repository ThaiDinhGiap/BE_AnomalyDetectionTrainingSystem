package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.ProcessClassification;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class TrainingResultProductOptionResponse {
    private Long id;
    private String name;
    private BigDecimal standardTimeJt;

    public TrainingResultProductOptionResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public TrainingResultProductOptionResponse(Long id, String name, BigDecimal standardTimeJt) {
        this.id = id;
        this.name = name;
        this.standardTimeJt = standardTimeJt;
    }
}
