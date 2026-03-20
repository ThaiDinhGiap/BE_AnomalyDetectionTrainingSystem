package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleProposalDetailResponse;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposalDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ProcessMapper.class, DefectMapper.class, TrainingSampleMapper.class})
public abstract class TrainingSampleProposalDetailMapper {

    @Mapping(target = "processName", source = "process.name")
    @Mapping(target = "processId", source = "process.id")
    @Mapping(target = "defectDescription", source = "defect.defectDescription")
    @Mapping(target = "defectId", source = "defect.id")
    @Mapping(target = "trainingSampleId", source = "trainingSample.id")
    @Mapping(target = "trainingSampleProposalId", source = "trainingSampleProposal.id")
    @Mapping(target = "trainingSampleProposalDetailId", source = "id")
    public abstract TrainingSampleProposalDetailResponse toResponse(TrainingSampleProposalDetail entity);
}
