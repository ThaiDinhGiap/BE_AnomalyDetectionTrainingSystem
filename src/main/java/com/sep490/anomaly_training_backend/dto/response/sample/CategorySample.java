package com.sep490.anomaly_training_backend.dto.response.sample;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategorySample {
    List<String> categoryNames;
    List<String> trainingDescriptions;
}
