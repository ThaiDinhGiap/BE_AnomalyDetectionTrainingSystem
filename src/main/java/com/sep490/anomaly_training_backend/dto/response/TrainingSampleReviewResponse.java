package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.TrainingSampleReviewResult;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.TrainingSampleReviewConfig;
import com.sep490.anomaly_training_backend.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
