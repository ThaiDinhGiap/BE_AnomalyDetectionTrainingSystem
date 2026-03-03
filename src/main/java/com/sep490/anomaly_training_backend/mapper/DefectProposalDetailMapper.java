package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.response.DefectProposalDetailResponse;
import com.sep490.anomaly_training_backend.model.DefectProposalDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class DefectProposalDetailMapper {

//    @Mapping(target = "DefectProposalDescription", source = "defectDescription")
    @Mapping(target = "processName", source = "defect.process.name")
    public abstract DefectProposalDetailResponse toResponse(final DefectProposalDetail entity);
}
