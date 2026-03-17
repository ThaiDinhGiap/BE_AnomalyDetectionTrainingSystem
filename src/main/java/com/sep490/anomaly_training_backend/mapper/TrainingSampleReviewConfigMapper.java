package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.response.TrainingSampleReviewConfigResponse;
import com.sep490.anomaly_training_backend.model.TrainingSampleReviewConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class TrainingSampleReviewConfigMapper {
    public abstract TrainingSampleReviewConfigResponse toDto(TrainingSampleReviewConfig entity);
}
