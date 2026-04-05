package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleResponse;
import com.sep490.anomaly_training_backend.model.TrainingSample;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ProductMapper.class, DefectMapper.class})
public abstract class TrainingSampleMapper {

    @Mapping(target = "processId", source = "process.id")
    @Mapping(target = "processName", source = "process.name")
    @Mapping(target = "trainingSampleId", source = "id")
    @Mapping(target = "productLineId", source = "productLine.id")
    public abstract TrainingSampleResponse toDto(TrainingSample entity);
}
