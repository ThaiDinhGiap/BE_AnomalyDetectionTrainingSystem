package com.sep490.anomaly_training_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ExpiringSkillResponse {
    private long count;
    private int daysThreshold;
    private String description;
    private List<String> employeeCodes;
}
