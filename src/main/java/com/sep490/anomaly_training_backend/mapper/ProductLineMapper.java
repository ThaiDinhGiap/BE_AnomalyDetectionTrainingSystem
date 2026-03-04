package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.response.ProductLineResponse;
import com.sep490.anomaly_training_backend.model.ProductLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",uses = {ProcessMapper.class })
public abstract class ProductLineMapper {
    @Mapping(source = "group.id", target = "groupId")
    public abstract ProductLineResponse toDto(ProductLine productLine);
}
