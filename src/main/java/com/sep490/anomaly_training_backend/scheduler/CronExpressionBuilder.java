package com.sep490.anomaly_training_backend.scheduler;

import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;

public class CronExpressionBuilder {

    public static String buildCronExpression(Integer triggerDay, Integer triggerMonth) {
        validateInputs(triggerDay, triggerMonth);
        return String.format("0 0 0 %d %d ? *", triggerDay, triggerMonth);
    }

    private static void validateInputs(Integer triggerDay, Integer triggerMonth) {
        if (triggerDay == null || triggerDay < 1 || triggerDay > 31) {
            throw new AppException(ErrorCode.INVALID_REQUEST_FORMAT, "Invalid trigger day");
        }

        if (triggerMonth == null || triggerMonth < 1 || triggerMonth > 12) {
            throw new AppException(ErrorCode.INVALID_REQUEST_FORMAT,  "Invalid trigger month");
        }

    }
}
