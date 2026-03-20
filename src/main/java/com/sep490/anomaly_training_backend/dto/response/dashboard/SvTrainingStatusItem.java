package com.sep490.anomaly_training_backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Item in TrainingStatusAccordion donut chart.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SvTrainingStatusItem {
    private String name;    // "Đã lên lịch huấn luyện" / "Đã huấn luyện" / "Trễ lịch huấn luyện"
    private int value;
    private String color;   // "#3b82f6" / "#22c55e" / "#f43f5e"
}
