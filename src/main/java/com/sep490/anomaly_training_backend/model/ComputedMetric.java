package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.ComputeMethod;
import com.sep490.anomaly_training_backend.enums.MetricReturnType;
import com.sep490.anomaly_training_backend.enums.PolicyEntityType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "computed_metrics")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ComputedMetric extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "metric_name", length = 50, unique = true, nullable = false)
    String metricName;

    @Column(name = "display_name", length = 100, nullable = false)
    String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    PolicyEntityType entityType;

    @Enumerated(EnumType.STRING)
    @Column(name = "compute_method", nullable = false)
    ComputeMethod computeMethod;

    @Column(name = "compute_definition", length = 2000, nullable = false)
    String computeDefinition;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_type", nullable = false)
    MetricReturnType returnType;

    @Column(name = "unit", length = 20)
    String unit;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;
}
