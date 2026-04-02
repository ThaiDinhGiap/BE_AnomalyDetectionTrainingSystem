package com.sep490.anomaly_training_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FailedTrainingResponse {
    private long count;
    private String description;
    private List<Long> resultIds;
    private List<String> employeeCodes;
}
