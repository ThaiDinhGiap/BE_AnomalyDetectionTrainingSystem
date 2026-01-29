package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.response.DefectReportDetailResponse;
import com.sep490.anomaly_training_backend.model.DefectReportDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class DefectReportDetailMapper {

    @Mapping(target = "defectReportDescription", source = "defectDescription")
    @Mapping(target = "processName", source = "defect.process.name")
    public abstract DefectReportDetailResponse toResponse(final DefectReportDetail entity);
}
