package com.sep490.anomaly_training_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Returned when the TL opens an entity that was rejected,
 * so they can see the exact structured feedback.
 */
@Data
@Builder
public class RejectionDetailResponse {
    private Long actionLogId;
    private Instant rejectedAt;
    private String rejectedByUsername;
    private String rejectedByFullName;

    /**
     * The structured reasons that were checked
     */
    private List<RejectReasonGroupResponse> reasonsByCategory;

    /**
     * The required action that was selected
     */
    private String requiredActionName;

    /**
     * Free-text comment
     */
    private String comment;
}
