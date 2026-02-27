package com.sep490.anomaly_training_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * Entity for training_sample_review_configs table - Configuration for periodic reviews
 */
@Entity
@Table(name = "training_sample_review_configs", uniqueConstraints = {
        @UniqueConstraint(name = "uk_review_configs_product_line", columnNames = {"product_line_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingSampleReviewConfig extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_line_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    ProductLine productLine;

    @Column(name = "trigger_month", nullable = false)
    @Builder.Default
    Integer triggerMonth = 3;

    @Column(name = "trigger_day", nullable = false)
    @Builder.Default
    Integer triggerDay = 1;

    @Column(name = "due_days", nullable = false)
    @Builder.Default
    Integer dueDays = 30;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    User assignee;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    Boolean isActive = true;
}
