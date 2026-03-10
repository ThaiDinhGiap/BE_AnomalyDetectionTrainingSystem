package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.request.UserRequest;
import com.sep490.anomaly_training_backend.dto.response.UserDashboard;
import com.sep490.anomaly_training_backend.dto.response.UserResponse;
import com.sep490.anomaly_training_backend.model.Role;
import com.sep490.anomaly_training_backend.model.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    UserResponse toDTO(User user);

    @Mapping(target = "roles", source = "roles")
    UserDashboard toUserDashboard(User user);

    @Mapping(target = "passwordHash", ignore = true)
    User toEntity(UserRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "passwordHash", ignore = true)
    void updateEntity(@MappingTarget User user, UserRequest dto);

    default List<String> map(Set<Role> roles) {
        if (roles == null) return List.of();
        return roles.stream()
                .map(Role::getRoleCode)
                .toList();
    }
}