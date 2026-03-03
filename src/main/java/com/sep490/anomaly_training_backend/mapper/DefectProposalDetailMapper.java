package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.response.DefectProposalDetailResponse;
import com.sep490.anomaly_training_backend.model.DefectProposalDetail;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public abstract class DefectProposalDetailMapper {

    @Mapping(target = "defectProposalDescription", source = "defect.defectDescription")
    @Mapping(target = "processName", source = "defect.process.name")
    public abstract DefectProposalDetailResponse toResponse(final DefectProposalDetail entity);
}
