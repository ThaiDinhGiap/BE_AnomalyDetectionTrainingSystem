package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleReviewConfigResponse;
import com.sep490.anomaly_training_backend.model.TrainingSampleReviewConfig;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class TrainingSampleReviewConfigMapper {
    public abstract TrainingSampleReviewConfigResponse toDto(TrainingSampleReviewConfig entity);
}
