package com.sep490.anomaly_training_backend.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportErrorItem {
    private Integer rowNumber;
    private String field;
    private String value;
    private String message;
}
