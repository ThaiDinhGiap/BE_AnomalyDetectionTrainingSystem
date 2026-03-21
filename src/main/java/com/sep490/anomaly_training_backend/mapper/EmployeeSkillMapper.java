package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.response.EmployeeSkillResponse;
import com.sep490.anomaly_training_backend.model.EmployeeSkill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring", uses = {ProcessMapper.class, EmployeeMapper.class})
@Component
public interface EmployeeSkillMapper {

    @Mapping(source = "employee.fullName", target = "employeeName")
    @Mapping(source = "employee.employeeCode", target = "employeeCode")
    EmployeeSkillResponse toDto(EmployeeSkill employeeSkill);

}
