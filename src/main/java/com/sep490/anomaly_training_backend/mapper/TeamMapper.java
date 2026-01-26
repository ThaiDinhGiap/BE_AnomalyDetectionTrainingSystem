package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.request.TeamRequest;
import com.sep490.anomaly_training_backend.dto.response.TeamResponse;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.GroupRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class TeamMapper {

    @Autowired
    protected GroupRepository groupRepository;

    @Autowired
    protected UserRepository userRepository;

    // 1. Entity -> DTO
    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "groupName", source = "group.name")
    @Mapping(target = "teamLeaderId", source = "teamLeader.id")
    @Mapping(target = "teamLeaderName", source = "teamLeader.fullName")
    public abstract TeamResponse toDTO(Team team);

    // 2. DTO -> Entity (Create)
    @Mapping(target = "group", source = "groupId", qualifiedByName = "mapGroupById")
    @Mapping(target = "teamLeader", source = "teamLeaderId", qualifiedByName = "mapUserById")
    public abstract Team toEntity(TeamRequest dto);

    // 3. Update Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "group", source = "groupId", qualifiedByName = "mapGroupById")
    @Mapping(target = "teamLeader", source = "teamLeaderId", qualifiedByName = "mapUserById")
    public abstract void updateEntity(@MappingTarget Team team, TeamRequest dto);

    // --- Helper Methods ---

    @Named("mapGroupById")
    Group mapGroupById(Long id) {
        if (id == null) return null; // Hoặc throw exception nếu logic bắt buộc
        return groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found id: " + id));
    }

    @Named("mapUserById")
    User mapUserById(Long id) {
        if (id == null) return null;
        return userRepository.findById(id).orElse(null);
    }
//    protected Instant map(LocalDateTime localDateTime) {
//        if (localDateTime == null) return null;
//        // Chuyển LocalDateTime sang Instant dựa trên múi giờ hệ thống (System Default)
//        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
//    }
}