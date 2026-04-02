package com.sep490.anomaly_training_backend.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private List<String> attachmentUrls;
    private List<ProcessResponse> processes;
}

