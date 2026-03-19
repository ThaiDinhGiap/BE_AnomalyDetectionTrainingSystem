package com.sep490.anomaly_training_backend.dto.response;


import com.sep490.anomaly_training_backend.enums.ProcessClassification;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TrainingResultProcessResponse {
    private Long id;
    private String name;
    private Integer classification;

    public TrainingResultProcessResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public TrainingResultProcessResponse(Long id, String name, Integer classification) {
        this.id = id;
        this.name = name;
        this.classification = classification;
    }
}
