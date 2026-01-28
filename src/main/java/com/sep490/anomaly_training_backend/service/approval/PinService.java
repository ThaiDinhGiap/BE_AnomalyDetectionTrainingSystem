package com.sep490.anomaly_training_backend.service.approval;

import com.sep490.anomaly_training_backend.model.User;

public interface PinService {

    /**
     * Thiết lập PIN mới cho user (lần đầu hoặc đổi PIN)
     */
    void setupPin(User user, String pin);

    /**
     * Xác thực PIN của user
     *
     * @throws BusinessException nếu PIN sai, bị khóa, hoặc hết hạn
     */
    void verifyPin(User user, String pin);

    /**
     * Kiểm tra user đã có PIN chưa
     */
    boolean hasPin(User user);

    /**
     * Đổi PIN (cần verify PIN cũ trước)
     */
    void changePin(User user, String oldPin, String newPin);
}