package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.request.ProcessQualificationRequest;
import com.sep490.anomaly_training_backend.dto.response.ProcessQualificationResponse;
import com.sep490.anomaly_training_backend.model.ProcessQualification;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProcessQualificationMapper {


    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.fullName")
    @Mapping(target = "employeeCode", source = "employee.employeeCode")

    @Mapping(target = "processId", source = "process.id")
    @Mapping(target = "processName", source = "process.name")
    @Mapping(target = "processCode", source = "process.code")
    ProcessQualificationResponse toDTO(ProcessQualification entity);

    @Mapping(target = "employee.id", source = "employeeId")
    @Mapping(target = "process.id", source = "processId")
    ProcessQualification toEntity(ProcessQualificationRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "employee.id", source = "employeeId")
    @Mapping(target = "process.id", source = "processId")
    void updateEntity(@MappingTarget ProcessQualification entity, ProcessQualificationRequest dto);
}