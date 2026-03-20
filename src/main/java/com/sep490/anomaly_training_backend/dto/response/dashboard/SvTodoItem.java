package com.sep490.anomaly_training_backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SvTodoItem {
    private Long id;
    private String entityType;   // TRAINING_PLAN, TRAINING_RESULT, DEFECT_PROPOSAL, TRAINING_SAMPLE_PROPOSAL
    private String title;        // "Kế hoạch đào tạo: ..."
    private String senderName;   // "Nguyễn Văn A (Team Lead)"
    private String waitTime;     // "2 giờ", "3 ngày"
    private String status;       // WAITING_SV
}
