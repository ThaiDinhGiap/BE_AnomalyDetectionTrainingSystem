package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.response.TrainingTopicResponse;
import com.sep490.anomaly_training_backend.model.TrainingTopic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class TrainingTopicMapper {
    @Mapping(target = "processId", source = "process.id")
    @Mapping(target = "processName", source = "process.name")
    @Mapping(target = "defectId", source = "defect.id")
    @Mapping(target = "defectName", source = "defect.id")
    public abstract TrainingTopicResponse toDto(TrainingTopic entity);
}
