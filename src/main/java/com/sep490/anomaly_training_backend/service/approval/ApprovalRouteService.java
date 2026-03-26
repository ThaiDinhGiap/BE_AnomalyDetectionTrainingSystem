package com.sep490.anomaly_training_backend.service.approval;

import com.sep490.anomaly_training_backend.model.User;

public interface ApprovalRouteService {

    /**
     * Xác định user ID của người duyệt cho step này dựa trên group_id và permission
     */
    Long getApproverIdForStep(Long groupId, String requiredPermission);

    /**
     * Lấy user object của người duyệt
     */
    User getApproverForStep(Long groupId, String requiredPermission);

    /**
     * Kiểm tra user có phải là approver hợp lệ cho group này không
     */
    boolean isValidApprover(Long groupId, String requiredPermission, Long userId);
}