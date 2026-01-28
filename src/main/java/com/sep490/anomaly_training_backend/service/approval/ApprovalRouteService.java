package com.sep490.anomaly_training_backend.service.approval;

import com.sep490.anomaly_training_backend.enums.UserRole;
import com.sep490.anomaly_training_backend.model.User;

public interface ApprovalRouteService {

    /**
     * Xác định user ID của người duyệt cho step này dựa trên group_id
     */
    Long getApproverIdForStep(Long groupId, UserRole approverRole);

    /**
     * Lấy user object của người duyệt
     */
    User getApproverForStep(Long groupId, UserRole approverRole);

    /**
     * Kiểm tra user có phải là approver hợp lệ cho group này không
     */
    boolean isValidApprover(Long groupId, UserRole approverRole, Long userId);
}