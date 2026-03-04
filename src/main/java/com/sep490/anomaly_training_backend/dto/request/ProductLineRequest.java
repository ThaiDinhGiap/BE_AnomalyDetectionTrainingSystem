package com.sep490.anomaly_training_backend.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductLineRequest {
    String name;
    Long groupId;
}
