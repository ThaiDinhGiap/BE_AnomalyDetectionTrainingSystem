package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.enums.TrainingSampleReviewResult;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity for training_sample_reviews table - Each review log with JSON snapshot
 */
@Entity
@Table(name = "training_sample_reviews", uniqueConstraints = {
        @UniqueConstraint(name = "uk_reviews_period", columnNames = {"product_line_id", "review_date"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingSampleReview extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "config_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    TrainingSampleReviewConfig config;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_line_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    ProductLine productLine;

    @Column(name = "review_date", nullable = false)
    LocalDate reviewDate;

    @Column(name = "due_date", nullable = false)
    LocalDate dueDate;

    @Column(name = "completed_date")
    LocalDate completedDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewed_by")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    User reviewedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    TrainingSampleReviewResult result = TrainingSampleReviewResult.PENDING;

    @Column(name = "sample_snapshot", columnDefinition = "json")
    String sampleSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_by")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    User confirmedBy;

}
