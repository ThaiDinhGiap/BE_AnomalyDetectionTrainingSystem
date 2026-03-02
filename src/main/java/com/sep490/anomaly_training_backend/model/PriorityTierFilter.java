package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.FilterOperator;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "priority_tier_filters")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"tier"})
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class PriorityTierFilter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tier_id")
    @ToString.Exclude
    PriorityTier tier;

    @Column(name = "metric_name", length = 50, nullable = false)
    String metricName;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator", nullable = false)
    FilterOperator operator;

    @Column(name = "filter_value", length = 100, nullable = false)
    String filterValue;

    @Column(name = "filter_unit", length = 20)
    String filterUnit;

    @Column(name = "filter_order", nullable = false)
    @Builder.Default
    Integer filterOrder = 0;
}
