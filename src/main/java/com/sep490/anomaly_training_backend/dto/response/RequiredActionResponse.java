package com.sep490.anomaly_training_backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequiredActionResponse {
    private Long id;
    private String actionName;
}
