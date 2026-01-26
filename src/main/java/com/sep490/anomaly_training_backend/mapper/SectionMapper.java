package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.request.SectionRequest;
import com.sep490.anomaly_training_backend.dto.response.SectionResponse;
import com.sep490.anomaly_training_backend.model.Section;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class SectionMapper {

    @Autowired
    protected UserRepository userRepository;

    // 1. Entity -> DTO
    @Mapping(target = "managerId", source = "manager.id")
    @Mapping(target = "managerName", source = "manager.fullName")
    public abstract SectionResponse toDTO(Section section);

    // 2. DTO -> Entity (Create)
    // Tự động tìm User từ managerId thông qua hàm mapUserById
    @Mapping(target = "manager", source = "managerId", qualifiedByName = "mapUserById")
    public abstract Section toEntity(SectionRequest dto);

    // 3. Update Entity (Edit)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "manager", source = "managerId", qualifiedByName = "mapUserById")
    public abstract void updateEntity(@MappingTarget Section section, SectionRequest dto);

    // --- Helper Method ---
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