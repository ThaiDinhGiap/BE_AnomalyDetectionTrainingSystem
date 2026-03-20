package com.sep490.anomaly_training_backend.mapper;


import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleReviewResponse;
import com.sep490.anomaly_training_backend.model.TrainingSampleReview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ProductLineMapper.class})
public abstract class TrainingSampleReviewMapper {

    @Mapping(target = "productLine", source = "productLine.name")
    @Mapping(target = "reviewedBy", source = "reviewedBy.fullName")
    @Mapping(target = "confirmedBy", source = "confirmedBy.fullName")
    public abstract TrainingSampleReviewResponse toDto(TrainingSampleReview entity);
}
