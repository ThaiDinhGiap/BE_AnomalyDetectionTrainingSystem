package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.response.TrainingTopicReportResponse;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposal;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import java.util.Objects;

@Mapper(componentModel = "spring")
@RequiredArgsConstructor
public abstract class TrainingTopicReportMapper {

//    @Mapping(target = "groupName", source = "group.name")
    @Mapping(target = "createdDate", source = "updatedAt")
    @Mapping(target = "teamLeadId", source = "createdBy", qualifiedByName = "usernameToId")
    @Mapping(target = "teamLeadName", source = "createdBy", qualifiedByName = "usernameToName")
    public abstract TrainingTopicReportResponse toResponse(TrainingSampleProposal entity,
                                                           @Context UserRepository userRepository);

    @Named("usernameToId")
    protected Long usernameToId(String username, @Context UserRepository userRepository) {
        if (username == null) return null;
        return Objects.requireNonNull(userRepository.findByUsername(username).orElse(null)).getId();
    }

    @Named("usernameToName")
    protected String usernameToName(String username, @Context UserRepository userRepository) {
        if (username == null) return null;
        return Objects.requireNonNull(userRepository.findByUsername(username).orElse(null)).getFullName();
    }
}
