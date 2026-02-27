package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.request.ProcessRequest;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.repository.ProductLineRepository;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ProcessMapper {

    @Autowired
    protected ProductLineRepository productLineRepository;

    // 1. Entity -> DTO
    @Mapping(target = "productLineId", source = "productLine.id")
    @Mapping(target = "productLineName", source = "productLine.name")
    public abstract ProcessResponse toDTO(Process entity);

    // 2. DTO -> Entity (Create)
    @Mapping(target = "productLine", source = "productLineId", qualifiedByName = "mapProductLineById")
    public abstract Process toEntity(ProcessRequest dto);

    // 3. Update Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "productLine", source = "productLineId", qualifiedByName = "mapProductLineById")
    public abstract void updateEntity(@MappingTarget Process entity, ProcessRequest dto);

    // --- Helper Method ---
    @Named("mapProductLineById")
    ProductLine mapProductLineById(Long id) {
        if (id == null) return null;
        return productLineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductLine not found id: " + id));
    }
}