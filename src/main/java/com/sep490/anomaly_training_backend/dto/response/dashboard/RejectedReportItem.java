package com.sep490.anomaly_training_backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectedReportItem {
    private Long id;
    private Integer type;       // 1: Kế hoạch, 2: Kết quả, 3: Lỗi, 4: Mẫu
    private String title;
    private String description;
    private String entityType;  // TRAINING_PLAN, TRAINING_RESULT, DEFECT_PROPOSAL, TRAINING_SAMPLE_PROPOSAL
}
