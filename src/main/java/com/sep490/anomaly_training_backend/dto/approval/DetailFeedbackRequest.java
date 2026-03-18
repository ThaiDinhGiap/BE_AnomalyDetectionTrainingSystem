package com.sep490.anomaly_training_backend.dto.approval;

import lombok.Data;

import java.util.List;

@Data
public class DetailFeedbackRequest {

    private List<Long> rejectReasonIds;

    private Long requiredActionId;

    private String comment;
}
