package com.sep490.anomaly_training_backend.service.approval;

import com.sep490.anomaly_training_backend.dto.response.RejectReasonGroupResponse;
import com.sep490.anomaly_training_backend.dto.response.RequiredActionResponse;

import java.util.List;

/**
 * Provides read-only metadata needed by the rejection form UI:
 * - Reject reasons (grouped by category for the checkbox grid)
 * - Required actions (for the radio group)
 */
public interface ApprovalMetadataService {

    /**
     * Returns all reject reasons grouped by categoryName.
     * One entry per category; each entry contains the list of reasons in that category.
     */
    List<RejectReasonGroupResponse> getRejectReasonGroups();

    /**
     * Returns all required actions as a flat list (shown as radio buttons).
     */
    List<RequiredActionResponse> getRequiredActions();
}