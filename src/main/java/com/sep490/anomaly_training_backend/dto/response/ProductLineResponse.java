package com.sep490.anomaly_training_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductLineResponse {
    Long id;
    Long groupId;
    String code;
    String name;
    List<ProcessResponse> processes;
}
