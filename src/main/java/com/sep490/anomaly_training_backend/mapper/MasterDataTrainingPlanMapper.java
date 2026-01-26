package com.sep490.anomaly_training_backend.mapper;
import com.sep490.anomaly_training_backend.dto.response.EmployeeSelectionResponse;
import com.sep490.anomaly_training_backend.dto.response.ProcessSelectionResponse;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.Process;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MasterDataTrainingPlanMapper {
    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "teamName", source = "team.name")
    EmployeeSelectionResponse toEmployeeResponse(Employee employee);

    List<EmployeeSelectionResponse> toEmployeeResponseList(List<Employee> employees);
    // Map Process -> DTO
    ProcessSelectionResponse toProcessResponse(Process process);
    List<ProcessSelectionResponse> toProcessResponseList(List<Process> processes);
}
