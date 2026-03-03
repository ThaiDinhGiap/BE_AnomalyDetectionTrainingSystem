package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.FilterLogic;
import com.sep490.anomaly_training_backend.enums.RankingDirection;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "priority_tiers")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"policy", "filters"})
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class PriorityTier extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id")
    @ToString.Exclude
    PriorityPolicy policy;

    @Column(name = "tier_order", nullable = false)
    Integer tierOrder;

    @Column(name = "tier_name", length = 100, nullable = false)
    String tierName;

    @Enumerated(EnumType.STRING)
    @Column(name = "filter_logic", nullable = false)
    @Builder.Default
    FilterLogic filterLogic = FilterLogic.AND;

    @Column(name = "ranking_metric", length = 50, nullable = false)
    String rankingMetric;

    @Enumerated(EnumType.STRING)
    @Column(name = "ranking_direction", nullable = false)
    RankingDirection rankingDirection;

    @Column(name = "secondary_metric", length = 50)
    String secondaryMetric;

    @Enumerated(EnumType.STRING)
    @Column(name = "secondary_direction")
    RankingDirection secondaryDirection;

    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;

    @OneToMany(mappedBy = "tier", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    List<PriorityTierFilter> filters = new ArrayList<>();
}
