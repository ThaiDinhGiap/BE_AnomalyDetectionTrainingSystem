package com.sep490.anomaly_training_backend.service.approval.impl;

import com.sep490.anomaly_training_backend.enums.UserRole;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.GroupRepository;
import com.sep490.anomaly_training_backend.service.approval.ApprovalRouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalRouteServiceImpl implements ApprovalRouteService {

    private final GroupRepository groupRepo;

    @Override
    public Long getApproverIdForStep(Long groupId, UserRole approverUserRole) {
        return getApproverForStep(groupId, approverUserRole).getId();
    }

    @Override
    public User getApproverForStep(Long groupId, UserRole approverUserRole) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

        return switch (approverUserRole) {
            case ROLE_SUPERVISOR -> group.getSupervisor();
            case ROLE_MANAGER -> group.getSection().getManager();
            default -> throw new AppException(ErrorCode.UNSUPPORTED_APPROVER_ROLE);
        };
    }

    @Override
    public boolean isValidApprover(Long groupId, UserRole approverUserRole, Long userId) {
        Long expectedApproverId = getApproverIdForStep(groupId, approverUserRole);
        return expectedApproverId.equals(userId);
    }
}