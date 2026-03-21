package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.response.ImportHistoryResponse;
import com.sep490.anomaly_training_backend.model.ImportHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public abstract class ImportHistoryMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.username")
    public abstract ImportHistoryResponse toDto(ImportHistory importHistory);
}
