package com.sep490.anomaly_training_backend.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SyncProductRequest {
    List<ProductRequest> request;
}
