package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleReviewPolicyResponse;
import com.sep490.anomaly_training_backend.model.TrainingSampleReviewPolicy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ProductLineMapper.class, TrainingSampleReviewConfigMapper.class})
public abstract class TrainingSampleReviewPolicyMapper {

    public abstract TrainingSampleReviewPolicyResponse toDto(TrainingSampleReviewPolicy entity);
}
