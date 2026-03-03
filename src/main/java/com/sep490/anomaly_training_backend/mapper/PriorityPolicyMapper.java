package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.scoring.ComputedMetricResponse;
import com.sep490.anomaly_training_backend.dto.scoring.PriorityPolicyListResponse;
import com.sep490.anomaly_training_backend.dto.scoring.PriorityPolicyResponse;
import com.sep490.anomaly_training_backend.dto.scoring.PriorityTierResponse;
import com.sep490.anomaly_training_backend.dto.scoring.TierFilterResponse;
import com.sep490.anomaly_training_backend.model.ComputedMetric;
import com.sep490.anomaly_training_backend.model.PriorityPolicy;
import com.sep490.anomaly_training_backend.model.PriorityTier;
import com.sep490.anomaly_training_backend.model.PriorityTierFilter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PriorityPolicyMapper {

    public PriorityPolicyResponse toResponse(PriorityPolicy policy) {
        if (policy == null) return null;
        return PriorityPolicyResponse.builder()
                .id(policy.getId())
                .policyCode(policy.getPolicyCode())
                .policyName(policy.getPolicyName())
                .entityType(policy.getEntityType())
                .effectiveDate(policy.getEffectiveDate())
                .expirationDate(policy.getExpirationDate())
                .status(policy.getStatus())
                .description(policy.getDescription())
                .tiers(policy.getTiers().stream()
                        .filter(t -> !t.isDeleteFlag())
                        .map(this::toTierResponse)
                        .collect(Collectors.toList()))
                .createdAt(policy.getCreatedAt())
                .createdBy(policy.getCreatedBy())
                .updatedAt(policy.getUpdatedAt())
                .updatedBy(policy.getUpdatedBy())
                .build();
    }

    public PriorityPolicyListResponse toListResponse(PriorityPolicy policy) {
        if (policy == null) return null;
        return PriorityPolicyListResponse.builder()
                .id(policy.getId())
                .policyCode(policy.getPolicyCode())
                .policyName(policy.getPolicyName())
                .entityType(policy.getEntityType())
                .status(policy.getStatus())
                .effectiveDate(policy.getEffectiveDate())
                .expirationDate(policy.getExpirationDate())
                .tierCount((int) policy.getTiers().stream().filter(t -> !t.isDeleteFlag()).count())
                .build();
    }

    public PriorityTierResponse toTierResponse(PriorityTier tier) {
        if (tier == null) return null;
        return PriorityTierResponse.builder()
                .id(tier.getId())
                .tierOrder(tier.getTierOrder())
                .tierName(tier.getTierName())
                .filterLogic(tier.getFilterLogic())
                .rankingMetric(tier.getRankingMetric())
                .rankingDirection(tier.getRankingDirection())
                .secondaryMetric(tier.getSecondaryMetric())
                .secondaryDirection(tier.getSecondaryDirection())
                .isActive(tier.getIsActive())
                .filters(tier.getFilters().stream()
                        .filter(f -> !f.isDeleteFlag())
                        .map(this::toFilterResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    public TierFilterResponse toFilterResponse(PriorityTierFilter filter) {
        if (filter == null) return null;
        return TierFilterResponse.builder()
                .id(filter.getId())
                .metricName(filter.getMetricName())
                .operator(filter.getOperator())
                .operatorSymbol(filter.getOperator() != null ? filter.getOperator().getSymbol() : null)
                .filterValue(filter.getFilterValue())
                .filterUnit(filter.getFilterUnit())
                .filterOrder(filter.getFilterOrder())
                .build();
    }

    public ComputedMetricResponse toMetricResponse(ComputedMetric metric) {
        if (metric == null) return null;
        return ComputedMetricResponse.builder()
                .id(metric.getId())
                .metricName(metric.getMetricName())
                .displayName(metric.getDisplayName())
                .entityType(metric.getEntityType())
                .computeMethod(metric.getComputeMethod())
                .returnType(metric.getReturnType())
                .unit(metric.getUnit())
                .description(metric.getDescription())
                .isActive(metric.getIsActive())
                .build();
    }

    public List<ComputedMetricResponse> toMetricResponseList(List<ComputedMetric> metrics) {
        if (metrics == null) return List.of();
        return metrics.stream().map(this::toMetricResponse).collect(Collectors.toList());
    }
}
