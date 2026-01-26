package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.request.GroupProductRequest;
import com.sep490.anomaly_training_backend.dto.response.GroupProductResponse;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.GroupProduct;
import com.sep490.anomaly_training_backend.repository.GroupRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class GroupProductMapper {

    @Autowired
    protected GroupRepository groupRepository;

    // 1. Entity -> DTO
    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "groupName", source = "group.name")
    public abstract GroupProductResponse toDTO(GroupProduct entity);

    // 2. DTO -> Entity (Create)
    @Mapping(target = "group", source = "groupId", qualifiedByName = "mapGroupById")
    public abstract GroupProduct toEntity(GroupProductRequest dto);

    // 3. Update Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "group", source = "groupId", qualifiedByName = "mapGroupById")
    public abstract void updateEntity(@MappingTarget GroupProduct entity, GroupProductRequest dto);

    // --- Helper Method ---
    @Named("mapGroupById")
    Group mapGroupById(Long id) {
        if (id == null) return null;
        return groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found id: " + id));
    }
//    protected Instant map(LocalDateTime localDateTime) {
//        if (localDateTime == null) return null;
//        // Chuyển LocalDateTime sang Instant dựa trên múi giờ hệ thống (System Default)
//        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
//    }
}