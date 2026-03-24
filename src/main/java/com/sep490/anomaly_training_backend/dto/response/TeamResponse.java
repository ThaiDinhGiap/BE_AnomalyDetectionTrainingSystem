package com.sep490.anomaly_training_backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamResponse {
    private Long id;
    private String name;
    private String code;

    private Long groupId;
    private String groupName;

    private Long teamLeaderId;
    private String teamLeaderName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}