package com.sep490.anomaly_training_backend.util;

import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReportUtils {
    public static String generateFormCode(ApprovalEntityType entityType, String groupName, Long entityId) {
        String prefix = groupName.trim().toUpperCase().replace(" ", "");

        // Format: TR_PLAN_LINE01_20250129_123
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateStr = LocalDate.now().format(formatter);

        switch (entityType) {
            case DEFECT_REPORT:
                prefix = "DEFECT_RP_" + prefix;
                break;
            case TRAINING_TOPIC_REPORT:
                prefix = "TOPIC_RP_" + prefix;
                break;
            case TRAINING_RESULT:
                prefix = "TR_RESULT_" + prefix;
                break;
            case TRAINING_PLAN:
                prefix = "TR_PLAN_" + prefix;
                break;
            default:
                break;
        }

        return String.format("%s_%s_%d", prefix, dateStr, entityId);
    }
}
