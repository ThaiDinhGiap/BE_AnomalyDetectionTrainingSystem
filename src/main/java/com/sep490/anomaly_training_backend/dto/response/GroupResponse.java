package com.sep490.anomaly_training_backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupResponse {
    private Long id;
    private String name;
    private String code;

    private Long sectionId;
    private String sectionName;

    private Long supervisorId;
    private String supervisorName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    List<TeamResponse> teams;

    public GroupResponse() {
    }

    public GroupResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}