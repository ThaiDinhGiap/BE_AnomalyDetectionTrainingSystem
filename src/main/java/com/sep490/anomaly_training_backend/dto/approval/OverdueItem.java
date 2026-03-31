package com.sep490.anomaly_training_backend.dto.approval;

/**
 * Generic item representing an overdue approval entity.
 * Used by ApprovalHandler.findOverdueItems() to provide entity-agnostic overdue data.
 */
public record OverdueItem(
        Long entityId,
        Long groupId
) {}
