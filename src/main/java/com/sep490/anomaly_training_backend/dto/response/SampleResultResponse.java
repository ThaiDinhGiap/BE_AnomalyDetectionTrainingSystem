package com.sep490.anomaly_training_backend.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SampleResultResponse {
    private Long id;
    private String code;
    private String name;
}
