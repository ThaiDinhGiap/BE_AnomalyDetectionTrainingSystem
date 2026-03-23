package com.sep490.anomaly_training_backend.dto.response.dashboard;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MngPendingApprovalItem {
    Long id;
    String entityType;       // TRAINING_PLAN, DEFECT_PROPOSAL, TRAINING_SAMPLE_PROPOSAL
    String title;
    String formCode;
    String submittedByName;
    String submittedAt;       // "2h trước", "5h trước"
    boolean isUrgent;         // > 24h waiting
}
