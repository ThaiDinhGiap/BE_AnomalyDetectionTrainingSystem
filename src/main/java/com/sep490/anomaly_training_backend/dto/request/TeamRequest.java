package com.sep490.anomaly_training_backend.dto.request;

import lombok.Data;

@Data
public class TeamRequest {
    private String name;

    private Long groupId;

    private String code;

    private Long teamLeaderId;
}