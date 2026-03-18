package com.sep490.anomaly_training_backend.service.approval.helper;

import com.sep490.anomaly_training_backend.enums.UserRole;
import com.sep490.anomaly_training_backend.model.Section;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExpectedApproverResolver {

    private final GroupRepository groupRepository;

    public Optional<User> resolve(Long groupId, UserRole role) {
        if (groupId == null) return Optional.empty();
        return groupRepository.findByIdAndDeleteFlagFalse(groupId)
                .flatMap(group -> switch (role) {
                    case ROLE_SUPERVISOR -> Optional.ofNullable(group.getSupervisor());
                    case ROLE_MANAGER -> Optional.ofNullable(group.getSection())
                            .map(Section::getManager);
                    default -> Optional.empty();
                });
    }
}
