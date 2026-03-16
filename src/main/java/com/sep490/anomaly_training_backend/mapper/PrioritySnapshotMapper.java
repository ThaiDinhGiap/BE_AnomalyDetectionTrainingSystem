package com.sep490.anomaly_training_backend.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep490.anomaly_training_backend.dto.scoring.PrioritySnapshotDetailDto;
import com.sep490.anomaly_training_backend.dto.scoring.PrioritySnapshotResponse;
import com.sep490.anomaly_training_backend.model.PrioritySnapshot;
import com.sep490.anomaly_training_backend.model.PrioritySnapshotDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class PrioritySnapshotMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "teamName", source = "team.name")
    @Mapping(target = "policyId", source = "policy.id")
    @Mapping(target = "policyCode", source = "policy.policyCode")
    @Mapping(target = "policyName", source = "policy.policyName")
    @Mapping(target = "totalEmployees", expression = "java(snapshot.getDetails() != null ? snapshot.getDetails().size() : 0)")
    @Mapping(target = "tierGroups", source = "details", qualifiedByName = "mapToTierGroups")
    public abstract PrioritySnapshotResponse toResponse(PrioritySnapshot snapshot);

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "metricValues", source = "metricValues", qualifiedByName = "jsonToMap")
    @Mapping(target = "priorityTags", source = "priorityTags", qualifiedByName = "jsonToMapString")
    public abstract PrioritySnapshotDetailDto toDetailDto(PrioritySnapshotDetail detail);

    @Named("mapToTierGroups")
    protected List<PrioritySnapshotResponse.TierGroupDto> mapToTierGroups(List<PrioritySnapshotDetail> details) {
        if (details == null || details.isEmpty()) return Collections.emptyList();

        // Nhóm các detail theo TierOrder và TierName
        return details.stream()
                .collect(Collectors.groupingBy(PrioritySnapshotDetail::getTierOrder))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // Sắp xếp theo thứ tự Tier (1, 2, 3...)
                .map(entry -> {
                    List<PrioritySnapshotDetail> tierDetails = entry.getValue();
                    String tierName = tierDetails.get(0).getTierName();

                    return PrioritySnapshotResponse.TierGroupDto.builder()
                            .tierOrder(entry.getKey())
                            .tierName(tierName)
                            .employeeCount(tierDetails.size())
                            .details(tierDetails.stream()
                                    .map(this::toDetailDto)
                                    .collect(Collectors.toList()))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Named("jsonToMap")
    protected Map<String, Object> jsonToMap(String json) {
        try {
            return json == null ? null : objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    @Named("jsonToMapString")
    protected Map<String, String> jsonToMapString(String json) {
        try {
            return json == null ? null : objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}