package com.sep490.anomaly_training_backend.service.approval.helper;

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

    public Optional<User> resolve(Long groupId, String requiredPermission) {
        if (groupId == null) return Optional.empty();
        return groupRepository.findByIdAndDeleteFlagFalse(groupId)
                .flatMap(group -> {
                    // Kiểm tra supervisor trước
                    User supervisor = group.getSupervisor();
                    if (supervisor != null && supervisor.hasPermission(requiredPermission)) {
                        return Optional.of(supervisor);
                    }
                    // Rồi đến manager
                    return Optional.ofNullable(group.getSection())
                            .map(Section::getManager)
                            .filter(manager -> manager.hasPermission(requiredPermission));
                });
    }
}
