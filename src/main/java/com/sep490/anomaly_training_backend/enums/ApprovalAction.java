package com.sep490.anomaly_training_backend.enums;

public enum ApprovalAction {
    REVISE,   // TL sửa lại sau reject (step_order = -1)
    SUBMIT,   // TL gửi lên (step_order = 0)
    APPROVE,  // SV/MG duyệt (step_order = 1, 2)
    REJECT    // SV/MG từ chối (step_order = 1, 2)
}