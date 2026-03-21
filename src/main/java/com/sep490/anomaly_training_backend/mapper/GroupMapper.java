package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.request.GroupRequest;
import com.sep490.anomaly_training_backend.dto.response.GroupResponse;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.Section;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.SectionRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {TeamMapper.class})
public abstract class GroupMapper {

    @Autowired
    protected SectionRepository sectionRepository;

    @Autowired
    protected UserRepository userRepository;

    // 1. Entity -> DTO
    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "sectionName", source = "section.name")
    @Mapping(target = "supervisorId", source = "supervisor.id")
    @Mapping(target = "supervisorName", source = "supervisor.fullName")
    public abstract GroupResponse toDTO(Group group);

    // 2. DTO -> Entity (Create)
    @Mapping(target = "section", source = "sectionId", qualifiedByName = "mapSectionById")
    @Mapping(target = "supervisor", source = "supervisorId", qualifiedByName = "mapUserById2")
    public abstract Group toEntity(GroupRequest dto);

    // 3. Update Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "section", source = "sectionId", qualifiedByName = "mapSectionById")
    @Mapping(target = "supervisor", source = "supervisorId", qualifiedByName = "mapUserById2")
    public abstract void updateEntity(@MappingTarget Group group, GroupRequest dto);

    // --- Helper Methods ---

    @Named("mapSectionById")
    Section mapSectionById(Long id) {
        if (id == null) return null; // Hoặc throw exception nếu bắt buộc
        return sectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Section not found id: " + id));
    }

    @Named("mapUserById2")
    User mapUserById2(Long id) {
        if (id == null) return null;
        return userRepository.findById(id).orElse(null);
    }
//    protected Instant map(LocalDateTime localDateTime) {
//        if (localDateTime == null) return null;
//        // Chuyển LocalDateTime sang Instant dựa trên múi giờ hệ thống (System Default)
//        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
//    }
}