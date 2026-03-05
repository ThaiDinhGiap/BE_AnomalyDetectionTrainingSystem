package com.sep490.anomaly_training_backend.service.approval.impl;

import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.service.approval.ApprovalHandler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApprovalHandlerRegistry {

    private final Map<ApprovalEntityType, ApprovalHandler> handlers;

    public ApprovalHandlerRegistry(List<ApprovalHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(
                        ApprovalHandler::getType,
                        h -> h
                ));
    }

    public ApprovalHandler getHandler(ApprovalEntityType type) {
        return handlers.get(type);
    }
}
