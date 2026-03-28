package com.sep490.anomaly_training_backend.service.approval.impl;

import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.Section;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.GroupRepository;
import com.sep490.anomaly_training_backend.service.approval.ApprovalRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;



@Service
@RequiredArgsConstructor
public class ApprovalRouteServiceImpl implements ApprovalRouteService {

    private final GroupRepository groupRepo;

    @Override
    public Long getApproverIdForStep(Long groupId, String requiredPermission) {
        return getApproverForStep(groupId, requiredPermission).getId();
    }

    @Override
    public User getApproverForStep(Long groupId, String requiredPermission) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

        // Tìm người duyệt trong org hierarchy dựa trên permission
        // Kiểm tra supervisor trước, rồi đến manager
        User supervisor = group.getSupervisor();
        if (supervisor != null && supervisor.hasPermission(requiredPermission)) {
            return supervisor;
        }

        if (group.getSection() != null) {
            User manager = group.getSection().getManager();
            if (manager != null && manager.hasPermission(requiredPermission)) {
                return manager;
            }
        }

        throw new AppException(ErrorCode.UNSUPPORTED_APPROVER_ROLE,
                "No approver found in org hierarchy with permission: " + requiredPermission);
    }

    @Override
    public boolean isValidApprover(Long groupId, String requiredPermission, Long userId) {
        try {
            Long expectedApproverId = getApproverIdForStep(groupId, requiredPermission);
            return expectedApproverId.equals(userId);
        } catch (AppException e) {
            return false;
        }
    }

    // ==================== EXPECTED APPROVER (merged from ExpectedApproverResolver) ====================

    @Override
    @Transactional(readOnly = true)
    public Optional<User> resolveExpectedApprover(Long groupId, String requiredPermission) {
        if (groupId == null) return Optional.empty();
        return groupRepo.findByIdAndDeleteFlagFalse(groupId)
                .flatMap(group -> {
                    User supervisor = group.getSupervisor();
                    if (supervisor != null && supervisor.hasPermission(requiredPermission)) {
                        return Optional.of(supervisor);
                    }
                    return Optional.ofNullable(group.getSection())
                            .map(Section::getManager)
                            .filter(manager -> manager.hasPermission(requiredPermission));
                });
    }
}