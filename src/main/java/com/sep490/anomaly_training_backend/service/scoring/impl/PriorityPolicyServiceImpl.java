package com.sep490.anomaly_training_backend.service.scoring.impl;

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
import com.sep490.anomaly_training_backend.exception.BusinessException;
import com.sep490.anomaly_training_backend.exception.ResourceNotFoundException;
import com.sep490.anomaly_training_backend.mapper.PriorityPolicyMapper;
import com.sep490.anomaly_training_backend.model.ComputedMetric;
import com.sep490.anomaly_training_backend.model.PriorityPolicy;
import com.sep490.anomaly_training_backend.model.PriorityTier;
import com.sep490.anomaly_training_backend.model.PriorityTierFilter;
import com.sep490.anomaly_training_backend.repository.ComputedMetricRepository;
import com.sep490.anomaly_training_backend.repository.PriorityPolicyRepository;
import com.sep490.anomaly_training_backend.service.scoring.PriorityPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            throw new BusinessException("Chỉ có thể chỉnh sửa chính sách ở trạng thái DRAFT");
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
    public Page<PriorityPolicyListResponse> listPolicies(PolicyEntityType entityType, PolicyStatus status, Pageable pageable) {
        Page<PriorityPolicy> page;
        if (entityType != null && status != null) {
            page = policyRepository.findByEntityTypeAndStatusAndDeleteFlagFalse(entityType, status, pageable);
        } else if (entityType != null) {
            page = policyRepository.findByEntityTypeAndDeleteFlagFalse(entityType, pageable);
        } else if (status != null) {
            page = policyRepository.findByStatusAndDeleteFlagFalse(status, pageable);
        } else {
            page = policyRepository.findByDeleteFlagFalse(pageable);
        }
        return page.map(mapper::toListResponse);
    }

    @Override
    @Transactional
    public void activatePolicy(Long id) {
        PriorityPolicy policy = findActivePolicy(id);
        if (policy.getStatus() != PolicyStatus.DRAFT) {
            throw new BusinessException("Chỉ có thể kích hoạt chính sách ở trạng thái DRAFT");
        }

        // Archive other active policies of same entity type
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
            throw new BusinessException("Chỉ có thể lưu trữ chính sách ở trạng thái ACTIVE");
        }
        policy.setStatus(PolicyStatus.ARCHIVED);
        policyRepository.save(policy);
    }

    @Override
    @Transactional
    public void deletePolicy(Long id) {
        PriorityPolicy policy = findActivePolicy(id);
        if (policy.getStatus() != PolicyStatus.DRAFT) {
            throw new BusinessException("Chỉ có thể xóa chính sách ở trạng thái DRAFT");
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

    // ---- Private helpers ----

    private PriorityPolicy findActivePolicy(Long id) {
        return policyRepository.findById(id)
                .filter(p -> !p.isDeleteFlag())
                .orElseThrow(() -> new ResourceNotFoundException("PriorityPolicy", "id", id));
    }

    private PolicyEntityType parseEntityType(String value) {
        try {
            return PolicyEntityType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Entity type không hợp lệ: " + value);
        }
    }

    private void validatePolicyDates(LocalDate effectiveDate, LocalDate expirationDate, boolean requireFutureOrToday) {
        if (requireFutureOrToday && effectiveDate.isBefore(LocalDate.now())) {
            throw new BusinessException("Ngày hiệu lực phải là hôm nay hoặc trong tương lai");
        }
        if (expirationDate != null && !expirationDate.isAfter(effectiveDate)) {
            throw new BusinessException("Ngày hết hạn phải sau ngày hiệu lực");
        }
    }

    private void validateTiers(List<PriorityTierRequest> tiers, PolicyEntityType entityType) {
        if (tiers == null || tiers.isEmpty()) {
            throw new BusinessException("Phải có ít nhất một tầng ưu tiên");
        }

        Set<String> validMetrics = computedMetricRepository
                .findByEntityTypeAndIsActiveTrueAndDeleteFlagFalse(entityType)
                .stream()
                .map(ComputedMetric::getMetricName)
                .collect(Collectors.toSet());

        for (int i = 0; i < tiers.size(); i++) {
            PriorityTierRequest tier = tiers.get(i);
            if (tier.getTierOrder() != i + 1) {
                throw new BusinessException("Thứ tự tầng phải liên tiếp bắt đầu từ 1");
            }
            if (!validMetrics.contains(tier.getRankingMetric())) {
                throw new BusinessException("Metric không hợp lệ cho entity type này: " + tier.getRankingMetric());
            }
            if (tier.getSecondaryMetric() != null && !validMetrics.contains(tier.getSecondaryMetric())) {
                throw new BusinessException("Secondary metric không hợp lệ: " + tier.getSecondaryMetric());
            }
            if (tier.getFilters() == null || tier.getFilters().isEmpty()) {
                throw new BusinessException("Tầng " + tier.getTierOrder() + " phải có ít nhất một bộ lọc");
            }
            for (TierFilterRequest filter : tier.getFilters()) {
                if (!validMetrics.contains(filter.getMetricName())) {
                    throw new BusinessException("Filter metric không hợp lệ: " + filter.getMetricName());
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
                // Fetch unit from computed_metrics
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
