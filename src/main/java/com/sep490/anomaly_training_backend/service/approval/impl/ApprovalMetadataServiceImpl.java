package com.sep490.anomaly_training_backend.service.approval.impl;

import com.sep490.anomaly_training_backend.dto.response.RejectReasonGroupResponse;
import com.sep490.anomaly_training_backend.dto.response.RejectReasonResponse;
import com.sep490.anomaly_training_backend.dto.response.RequiredActionResponse;
import com.sep490.anomaly_training_backend.model.RejectReason;
import com.sep490.anomaly_training_backend.repository.RejectReasonRepository;
import com.sep490.anomaly_training_backend.repository.RequiredActionRepository;
import com.sep490.anomaly_training_backend.service.approval.ApprovalMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApprovalMetadataServiceImpl implements ApprovalMetadataService {

    private final RejectReasonRepository rejectReasonRepo;
    private final RequiredActionRepository requiredActionRepo;

    @Override
    public List<RejectReasonGroupResponse> getRejectReasonGroups() {
        List<RejectReason> allReasons = rejectReasonRepo.findAllByOrderByCategoryNameAscIdAsc();

        // Group by categoryName preserving insertion order
        Map<String, List<RejectReasonResponse>> grouped = new LinkedHashMap<>();
        for (RejectReason r : allReasons) {
            grouped
                    .computeIfAbsent(r.getCategoryName(), k -> new ArrayList<>())
                    .add(RejectReasonResponse.builder()
                            .id(r.getId())
                            .reasonName(r.getReasonName())
                            .build());
        }

        return grouped.entrySet().stream()
                .map(e -> RejectReasonGroupResponse.builder()
                        .categoryName(e.getKey())
                        .reasons(e.getValue())
                        .build())
                .toList();
    }

    @Override
    public List<RequiredActionResponse> getRequiredActions() {
        return requiredActionRepo.findAll().stream()
                .map(a -> RequiredActionResponse.builder()
                        .id(a.getId())
                        .actionName(a.getActionName())
                        .build())
                .toList();
    }
}