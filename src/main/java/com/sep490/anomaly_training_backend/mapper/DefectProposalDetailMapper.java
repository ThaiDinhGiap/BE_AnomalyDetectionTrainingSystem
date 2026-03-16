package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.response.DefectProposalDetailResponse;
import com.sep490.anomaly_training_backend.model.DefectProposalDetail;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public abstract class DefectProposalDetailMapper {

    @Mapping(target = "defectId", source = "defect.id")
    @Mapping(target = "processName", source = "process.name")
    @Mapping(target = "processId", source = "process.id")
    public abstract DefectProposalDetailResponse toResponse(final DefectProposalDetail entity);
}
