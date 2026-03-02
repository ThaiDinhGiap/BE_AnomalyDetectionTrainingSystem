package com.sep490.anomaly_training_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "metric_classifications")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class MetricClassification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "classification_name", length = 50, nullable = false)
    String classificationName;

    @Column(name = "metric_source", length = 50, nullable = false)
    String metricSource;

    @Column(name = "condition_expression", length = 500, nullable = false)
    String conditionExpression;

    @Column(name = "output_level", nullable = false)
    Integer outputLevel;

    @Column(name = "output_label", length = 50)
    String outputLabel;

    @Column(name = "priority", nullable = false)
    Integer priority;

    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;
}
