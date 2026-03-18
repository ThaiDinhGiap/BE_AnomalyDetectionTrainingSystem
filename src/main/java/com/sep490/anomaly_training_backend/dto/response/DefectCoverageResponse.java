package com.sep490.anomaly_training_backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DefectCoverageResponse {
    Long totalTrainingSample;
    Long totalDefect;
    List<DefectResponse> defects;
    Double coverageRate;
}
