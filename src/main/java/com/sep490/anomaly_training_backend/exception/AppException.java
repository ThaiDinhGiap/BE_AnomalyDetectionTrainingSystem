package com.sep490.anomaly_training_backend.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    // Dùng khi muốn lấy câu thông báo mặc định trong Enum
    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // Dùng khi muốn linh hoạt ghi đè câu thông báo
    public AppException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}