package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.TrainingSampleReviewResult;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.TrainingSampleReviewConfig;
import com.sep490.anomaly_training_backend.model.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TrainingSampleReviewResponse {
    Long id;
    Long productLine;
    Long productName;
    Integer reviewYear;
    LocalDate dueDate;
    LocalDate completedDate;
    TrainingSampleReviewResult result;
    String sampleSnapshot;
    Long confirmedBy;
    LocalDateTime confirmedAt;
}
