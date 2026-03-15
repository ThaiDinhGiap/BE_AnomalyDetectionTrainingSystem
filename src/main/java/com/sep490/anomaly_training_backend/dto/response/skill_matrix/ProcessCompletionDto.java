package com.sep490.anomaly_training_backend.dto.response.skill_matrix;

import lombok.Data;

@Data
public class ProcessCompletionDto {
    private Long id;
    private String code;
    private String name;
    private String completionRate;

    public ProcessCompletionDto(Long id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }
}
