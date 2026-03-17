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
    private String type;        // "Kế hoạch huấn luyện", "Kết quả huấn luyện", "Lỗi quá khứ", "Mẫu huấn luyện"
    private String title;
    private String description;
    private String entityType;  // TRAINING_PLAN, TRAINING_RESULT, DEFECT_PROPOSAL, TRAINING_SAMPLE_PROPOSAL
}
