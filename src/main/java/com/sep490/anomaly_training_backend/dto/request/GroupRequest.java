package com.sep490.anomaly_training_backend.dto.request;

import lombok.Data;

@Data
public class GroupRequest {
    private String name;

    private Long sectionId;

    private String code;

    private Long supervisorId;
}