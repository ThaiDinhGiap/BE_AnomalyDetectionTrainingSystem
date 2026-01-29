package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.response.TrainingTopicReportDetailResponse;
import com.sep490.anomaly_training_backend.model.TrainingTopicReportDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class TrainingTopicReportDetailMapper {
    @Mapping(target = "processName", source = "process.name")
    @Mapping(target = "defectDescription", source = "defect.defectDescription")
    public abstract TrainingTopicReportDetailResponse toResponse(TrainingTopicReportDetail entity);
}
