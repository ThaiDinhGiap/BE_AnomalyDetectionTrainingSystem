package com.sep490.anomaly_training_backend.service.priority.impl;

import com.sep490.anomaly_training_backend.dto.scoring.ComputedMetricResponse;
import com.sep490.anomaly_training_backend.dto.scoring.PriorityPolicyListResponse;
import com.sep490.anomaly_training_backend.dto.scoring.PriorityPolicyRequest;
import com.sep490.anomaly_training_backend.dto.scoring.PriorityPolicyResponse;
import com.sep490.anomaly_training_backend.dto.scoring.PriorityTierRequest;
import com.sep490.anomaly_training_backend.dto.scoring.TierFilterRequest;
import com.sep490.anomaly_training_backend.enums.FilterLogic;
import com.sep490.anomaly_training_backend.enums.FilterOperator;
import com.sep490.anomaly_training_backend.enums.PolicyEntityType;
import com.sep490.anomaly_training_backend.enums.PolicyStatus;
import com.sep490.anomaly_training_backend.enums.RankingDirection;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.PriorityPolicyMapper;
import com.sep490.anomaly_training_backend.model.ComputedMetric;
import com.sep490.anomaly_training_backend.model.PriorityPolicy;
import com.sep490.anomaly_training_backend.model.PriorityTier;
import com.sep490.anomaly_training_backend.model.PriorityTierFilter;
import com.sep490.anomaly_training_backend.repository.ComputedMetricRepository;
import com.sep490.anomaly_training_backend.repository.PriorityPolicyRepository;
import com.sep490.anomaly_training_backend.service.priority.PriorityPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PriorityPolicyServiceImpl implements PriorityPolicyService {

    private final PriorityPolicyRepository policyRepository;
    private final ComputedMetricRepository computedMetricRepository;
    private final PriorityPolicyMapper mapper;

    @Override
    @Transactional
    public PriorityPolicyResponse createPolicy(PriorityPolicyRequest request) {
        PolicyEntityType entityType = parseEntityType(request.getEntityType());
        validatePolicyDates(request.getEffectiveDate(), request.getExpirationDate(), true);
        validateTiers(request.getTiers(), entityType);

        String policyCode = generatePolicyCode(entityType);

        PriorityPolicy policy = PriorityPolicy.builder()
                .policyCode(policyCode)
                .policyName(request.getPolicyName())
                .entityType(entityType)
                .effectiveDate(request.getEffectiveDate())
                .expirationDate(request.getExpirationDate())
                .description(request.getDescription())
                .status(PolicyStatus.DRAFT)
                .build();

        buildTiers(policy, request.getTiers(), entityType);
        PriorityPolicy saved = policyRepository.save(policy);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PriorityPolicyResponse updatePolicy(Long id, PriorityPolicyRequest request) {
        PriorityPolicy policy = findActivePolicy(id);
        if (policy.getStatus() != PolicyStatus.DRAFT) {
            throw new AppException(ErrorCode.INVALID_POLICY_STATUS, "Only DRAFT policies can be edited.");
        }

        PolicyEntityType entityType = parseEntityType(request.getEntityType());
        validatePolicyDates(request.getEffectiveDate(), request.getExpirationDate(), false);
        validateTiers(request.getTiers(), entityType);

        policy.setEntityType(entityType);
        policy.setPolicyName(request.getPolicyName());
        policy.setEffectiveDate(request.getEffectiveDate());
        policy.setExpirationDate(request.getExpirationDate());
        policy.setDescription(request.getDescription());

        policy.getTiers().clear();
        buildTiers(policy, request.getTiers(), entityType);

        PriorityPolicy saved = policyRepository.save(policy);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PriorityPolicyResponse getPolicy(Long id) {
        PriorityPolicy policy = findActivePolicy(id);
        return mapper.toResponse(policy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriorityPolicyListResponse> listPolicies(PolicyEntityType entityType, PolicyStatus status) {
        List<PriorityPolicy> priorityPolicies;
        if (entityType != null && status != null) {
            priorityPolicies = policyRepository.findByEntityTypeAndStatusAndDeleteFlagFalse(entityType, status);
        } else if (entityType != null) {
            priorityPolicies = policyRepository.findByEntityTypeAndDeleteFlagFalse(entityType);
        } else if (status != null) {
            priorityPolicies = policyRepository.findByStatusAndDeleteFlagFalse(status);
        } else {
            priorityPolicies = policyRepository.findByDeleteFlagFalse();
        }
        return priorityPolicies.stream().map(mapper::toListResponse).toList();
    }

    @Override
    @Transactional
    public void activatePolicy(Long id) {
        PriorityPolicy policy = findActivePolicy(id);
        if (policy.getStatus() != PolicyStatus.DRAFT) {
            throw new AppException(ErrorCode.INVALID_POLICY_STATUS, "Only DRAFT policies can be activated.");
        }

        List<PriorityPolicy> activePolicies = policyRepository
                .findByEntityTypeAndStatusAndDeleteFlagFalse(policy.getEntityType(), PolicyStatus.ACTIVE);
        for (PriorityPolicy active : activePolicies) {
            active.setStatus(PolicyStatus.ARCHIVED);
            policyRepository.save(active);
        }

        policy.setStatus(PolicyStatus.ACTIVE);
        policyRepository.save(policy);
    }

    @Override
    @Transactional
    public void archivePolicy(Long id) {
        PriorityPolicy policy = findActivePolicy(id);
        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new AppException(ErrorCode.INVALID_POLICY_STATUS, "Only ACTIVE policies can be archived.");
        }
        policy.setStatus(PolicyStatus.ARCHIVED);
        policyRepository.save(policy);
    }

    @Override
    @Transactional
    public void deletePolicy(Long id) {
        PriorityPolicy policy = findActivePolicy(id);
        if (policy.getStatus() != PolicyStatus.DRAFT) {
            throw new AppException(ErrorCode.INVALID_POLICY_STATUS, "Only DRAFT policies can be deleted.");
        }
        policy.setDeleteFlag(true);
        policyRepository.save(policy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComputedMetricResponse> getAvailableMetrics(PolicyEntityType entityType) {
        return mapper.toMetricResponseList(
                computedMetricRepository.findByEntityTypeAndIsActiveTrueAndDeleteFlagFalse(entityType)
        );
    }

    private PriorityPolicy findActivePolicy(Long id) {
        return policyRepository.findById(id)
                .filter(p -> !p.isDeleteFlag())
                .orElseThrow(() -> new AppException(ErrorCode.PRIORITY_POLICY_NOT_FOUND));
    }

    private PolicyEntityType parseEntityType(String value) {
        try {
            return PolicyEntityType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_ENTITY_TYPE, "Invalid entity type: " + value);
        }
    }

    private void validatePolicyDates(LocalDate effectiveDate, LocalDate expirationDate, boolean requireFutureOrToday) {
        if (requireFutureOrToday && effectiveDate.isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.INVALID_EFFECTIVE_DATE);
        }
        if (expirationDate != null && !expirationDate.isAfter(effectiveDate)) {
            throw new AppException(ErrorCode.INVALID_EXPIRATION_DATE);
        }
    }

    private void validateTiers(List<PriorityTierRequest> tiers, PolicyEntityType entityType) {
        if (tiers == null || tiers.isEmpty()) {
            throw new AppException(ErrorCode.MISSING_PRIORITY_TIER);
        }

        Set<String> validMetrics = computedMetricRepository
                .findByEntityTypeAndIsActiveTrueAndDeleteFlagFalse(entityType)
                .stream()
                .map(ComputedMetric::getMetricName)
                .collect(Collectors.toSet());

        for (int i = 0; i < tiers.size(); i++) {
            PriorityTierRequest tier = tiers.get(i);
            if (tier.getTierOrder() != i + 1) {
                throw new AppException(ErrorCode.INVALID_TIER_ORDER);
            }
            if (!validMetrics.contains(tier.getRankingMetric())) {
                throw new AppException(ErrorCode.INVALID_METRIC, "Invalid ranking metric: " + tier.getRankingMetric());
            }
            if (tier.getSecondaryMetric() != null && !validMetrics.contains(tier.getSecondaryMetric())) {
                throw new AppException(ErrorCode.INVALID_METRIC, "Invalid secondary metric: " + tier.getSecondaryMetric());
            }
            if (tier.getFilters() == null || tier.getFilters().isEmpty()) {
                throw new AppException(ErrorCode.MISSING_TIER_FILTER, "Tier " + tier.getTierOrder() + " must have at least one filter.");
            }
            for (TierFilterRequest filter : tier.getFilters()) {
                if (!validMetrics.contains(filter.getMetricName())) {
                    throw new AppException(ErrorCode.INVALID_METRIC, "Invalid filter metric: " + filter.getMetricName());
                }
            }
        }
    }

    private void buildTiers(PriorityPolicy policy, List<PriorityTierRequest> tierRequests, PolicyEntityType entityType) {
        List<PriorityTier> tiers = policy.getTiers();
        for (PriorityTierRequest req : tierRequests) {
            PriorityTier tier = PriorityTier.builder()
                    .policy(policy)
                    .tierOrder(req.getTierOrder())
                    .tierName(req.getTierName())
                    .filterLogic(FilterLogic.valueOf(req.getFilterLogic().toUpperCase()))
                    .rankingMetric(req.getRankingMetric())
                    .rankingDirection(RankingDirection.valueOf(req.getRankingDirection().toUpperCase()))
                    .secondaryMetric(req.getSecondaryMetric())
                    .secondaryDirection(req.getSecondaryDirection() != null
                            ? RankingDirection.valueOf(req.getSecondaryDirection().toUpperCase()) : null)
                    .build();

            List<PriorityTierFilter> filters = new ArrayList<>();
            int filterOrder = 0;
            for (TierFilterRequest filterReq : req.getFilters()) {
                String unit = computedMetricRepository.findByMetricNameAndDeleteFlagFalse(filterReq.getMetricName())
                        .map(ComputedMetric::getUnit)
                        .orElse(null);

                PriorityTierFilter filter = PriorityTierFilter.builder()
                        .tier(tier)
                        .metricName(filterReq.getMetricName())
                        .operator(FilterOperator.valueOf(filterReq.getOperator().toUpperCase()))
                        .filterValue(filterReq.getValue())
                        .filterUnit(unit)
                        .filterOrder(filterReq.getFilterOrder() != null ? filterReq.getFilterOrder() : filterOrder)
                        .build();
                filters.add(filter);
                filterOrder++;
            }
            tier.setFilters(filters);
            tiers.add(tier);
        }
    }

    private String generatePolicyCode(PolicyEntityType entityType) {
        String prefix = "PP-" + entityType.name() + "-";
        long nextNum = policyRepository.findMaxPolicyCodeByEntityTypeAndPrefix(entityType, prefix)
                .map(maxCode -> {
                    try {
                        return Long.parseLong(maxCode.substring(prefix.length())) + 1;
                    } catch (NumberFormatException e) {
                        return 1L;
                    }
                })
                .orElse(1L);
        String candidate = prefix + String.format("%03d", nextNum);
        while (policyRepository.existsByPolicyCodeAndDeleteFlagFalse(candidate)) {
            nextNum++;
            candidate = prefix + String.format("%03d", nextNum);
        }
        return candidate;
    }
}