package com.sep490.anomaly_training_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeamRequest {
    private String name;

    private Long groupId;

    private Long teamLeaderId;
}