package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.request.EmployeeRequest;
import com.sep490.anomaly_training_backend.dto.response.EmployeeResponse;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.repository.TeamRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class EmployeeMapper {

    @Autowired
    protected TeamRepository teamRepository;

    // 1. Entity -> DTO
    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "teamName", source = "team.name")
    public abstract EmployeeResponse toDTO(Employee employee);

    // 2. DTO -> Entity (Create)
    @Mapping(target = "team", source = "teamId", qualifiedByName = "mapTeamById")
    // Nếu request không gửi status thì để null (Entity sẽ tự set Default ACTIVE)
    public abstract Employee toEntity(EmployeeRequest dto);

    // 3. Update Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "team", source = "teamId", qualifiedByName = "mapTeamById")
    public abstract void updateEntity(@MappingTarget Employee employee, EmployeeRequest dto);

    // --- Helper Method ---
    @Named("mapTeamById")
    Team mapTeamById(Long id) {
        if (id == null) return null;
        return teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found id: " + id));
    }
//    protected Instant map(LocalDateTime localDateTime) {
//        if (localDateTime == null) return null;
//        // Chuyển LocalDateTime sang Instant dựa trên múi giờ hệ thống (System Default)
//        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
//    }
}