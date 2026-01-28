package com.sep490.anomaly_training_backend.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TrainingResultOptionResponse {
    private Long id;
    private String name;

    public TrainingResultOptionResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
