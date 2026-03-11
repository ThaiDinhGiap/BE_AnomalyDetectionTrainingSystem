package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.response.TrainingSampleResponse;
import com.sep490.anomaly_training_backend.model.TrainingSample;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class TrainingSampleMapper {
    @Mapping(target = "processId", source = "process.id")
    @Mapping(target = "processName", source = "process.name")
    @Mapping(target = "defectId", source = "defect.id")
    @Mapping(target = "defectDescription", source = "defect.defectDescription")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productCode", source = "product.code")
    @Mapping(target = "trainingSampleId", source = "id")
    public abstract TrainingSampleResponse toDto(TrainingSample entity);
}
