package com.sep490.anomaly_training_backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data point for TrainingROIAccordion composed chart (bar + line).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SvTrainingEffectivenessPoint {
    private String month;          // "Jan", "Feb", ...
    private int trainingHours;     // count of training sessions in month
    private int defects;           // count of defects in month
}
