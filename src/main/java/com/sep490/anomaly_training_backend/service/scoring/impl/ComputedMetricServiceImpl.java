package com.sep490.anomaly_training_backend.service.scoring.impl;

import com.sep490.anomaly_training_backend.dto.scoring.ComputedMetricResponse;
import com.sep490.anomaly_training_backend.enums.PolicyEntityType;
import com.sep490.anomaly_training_backend.mapper.PriorityPolicyMapper;
import com.sep490.anomaly_training_backend.repository.ComputedMetricRepository;
import com.sep490.anomaly_training_backend.service.scoring.ComputedMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComputedMetricServiceImpl implements ComputedMetricService {

    private final ComputedMetricRepository computedMetricRepository;
    private final PriorityPolicyMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ComputedMetricResponse> getMetricsByEntityType(PolicyEntityType entityType) {
        return mapper.toMetricResponseList(
                computedMetricRepository.findByEntityTypeAndIsActiveTrueAndDeleteFlagFalse(entityType)
        );
    }
}
