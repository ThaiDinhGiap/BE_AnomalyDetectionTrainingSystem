package com.sep490.anomaly_training_backend.service.approval.impl;

import com.sep490.anomaly_training_backend.enums.UserRole;
import com.sep490.anomaly_training_backend.exception.BusinessException;
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
                .orElseThrow(() -> new BusinessException("Không tìm thấy group: " + groupId));

        return switch (approverUserRole) {
            case ROLE_SUPERVISOR -> group.getSupervisor();
            case ROLE_MANAGER -> group.getSection().getManager();
            default -> throw new BusinessException("Unsupported approver UserRole: " + approverUserRole);
        };
    }

    @Override
    public boolean isValidApprover(Long groupId, UserRole approverUserRole, Long userId) {
        Long expectedApproverId = getApproverIdForStep(groupId, approverUserRole);
        return expectedApproverId.equals(userId);
    }
}