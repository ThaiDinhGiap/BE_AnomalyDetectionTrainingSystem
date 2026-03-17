package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.response.TrainingSampleReviewPolicyResponse;
import com.sep490.anomaly_training_backend.model.TrainingSampleReviewPolicy;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {ProductLineMapper.class, TrainingSampleReviewConfigMapper.class})
public abstract class TrainingSampleReviewPolicyMapper {

    public static final TrainingSampleReviewPolicyMapper INSTANCE = Mappers.getMapper(TrainingSampleReviewPolicyMapper.class);

    public abstract TrainingSampleReviewPolicyResponse toDto(TrainingSampleReviewPolicy entity);
}
