package com.sep490.anomaly_training_backend.service.approval;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.DetailFeedbackRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.model.Approvable;
import com.sep490.anomaly_training_backend.model.User;
import jakarta.servlet.http.HttpServletRequest;

public interface ApprovalService {

    /**
     * TL submit report để bắt đầu quy trình duyệt
     */
    void submit(Approvable entity, User currentUser, HttpServletRequest request);

    /**
     * TL revise report sau khi bị reject (tăng version, chuyển về DRAFT)
     */
    void revise(Approvable entity, User currentUser, HttpServletRequest request);

    /**
     * SV/Manager approve report
     */
    void approve(Approvable entity, User currentUser, ApproveRequest req, HttpServletRequest request);

    /**
     * SV/Manager reject report
     */
    void reject(Approvable entity, User currentUser, RejectRequest req, HttpServletRequest request);

    /**
     * Kiểm tra user có thể approve/reject entity này không
     */
    Boolean canApprove(Approvable entity, User user);

    // ── Reject detail feedback (merged from RejectDetailService) ──

    void saveFeedback(ApprovalEntityType entityType, Long detailId, DetailFeedbackRequest request, User currentUser);
}