package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.request.EmployeeRequest;
import com.sep490.anomaly_training_backend.dto.response.EmployeeResponse;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.repository.TeamRepository;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class EmployeeMapper {

    @Autowired
    protected TeamRepository teamRepository;

    @Mapping(target = "teamIds", source = "teams")
    @Mapping(target = "teamName", expression = "java(joinTeamNames(employee.getTeams()))")
    @Mapping(target = "groupName", expression = "java(joinGroupNames(employee.getTeams()))")
    @Mapping(target = "sectionName", ignore = true)
    public abstract EmployeeResponse toDTO(Employee employee);

    @Mapping(target = "teams", source = "teamIds", qualifiedByName = "mapIdsToTeams")
    public abstract Employee toEntity(EmployeeRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "teams", source = "teamIds", qualifiedByName = "mapIdsToTeams")
    public abstract void updateEntity(@MappingTarget Employee employee, EmployeeRequest dto);

    @Named("mapIdsToTeams")
    protected List<Team> mapIdsToTeams(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        List<Team> teams = teamRepository.findAllById(ids);
        if (teams.size() != ids.size()) {
            throw new RuntimeException("Một hoặc nhiều Team ID không tồn tại");
        }
        return teams;
    }

    protected List<Long> mapTeamsToIds(List<Team> teams) {
        if (teams == null) return Collections.emptyList();
        return teams.stream().map(Team::getId).collect(Collectors.toList());
    }

    protected String joinTeamNames(List<Team> teams) {
        if (teams == null || teams.isEmpty()) return "";
        return teams.stream().map(Team::getName).collect(Collectors.joining(", "));
    }

    protected String joinGroupNames(List<Team> teams) {
        if (teams == null || teams.isEmpty()) return "";
        return teams.stream()
                .map(t -> t.getGroup() != null ? t.getGroup().getName() : "")
                .distinct()
                .filter(name -> !name.isEmpty())
                .collect(Collectors.joining(", "));
    }
}