package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalDetailResponse;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposalDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class TrainingSampleProposalDetailMapper {
    @Mapping(target = "processName", source = "process.name")
    @Mapping(target = "defectDescription", source = "defect.defectDescription")
    @Mapping(target = "trainingSample", source = "trainingSample.trainingDescription")
    public abstract TrainingSampleProposalDetailResponse toResponse(TrainingSampleProposalDetail entity);
}
