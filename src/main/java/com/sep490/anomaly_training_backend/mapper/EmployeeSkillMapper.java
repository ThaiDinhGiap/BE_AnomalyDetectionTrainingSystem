package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.response.EmployeeSkillResponse;
import com.sep490.anomaly_training_backend.model.EmployeeSkill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeeSkillMapper {

    @Mapping(source = "employee.fullName", target = "employeeName")
    @Mapping(source = "employee.employeeCode", target = "employeeCode")
    @Mapping(source = "employee.status", target = "status")
    @Mapping(source = "process.id", target = "processId")
    EmployeeSkillResponse toDto(EmployeeSkill employeeSkill);

}
