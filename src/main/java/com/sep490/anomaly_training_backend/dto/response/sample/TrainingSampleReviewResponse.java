package com.sep490.anomaly_training_backend.dto.response.sample;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrainingSampleReviewResponse {
    Long id;
    String productLine;
    LocalDate reviewDate;
    LocalDate dueDate;
    LocalDate completedDate;
    String reviewedBy;
    String result;
    String sampleSnapshot;
    String confirmedBy;
}
