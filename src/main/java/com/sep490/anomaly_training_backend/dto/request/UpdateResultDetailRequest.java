package com.sep490.anomaly_training_backend.dto.request;

import lombok.Data;
import java.time.LocalTime;

@Data
public class UpdateResultDetailRequest {
    private Long id;

    private Long productGroupId;

    private Long trainingTopicId;
    private String trainingSample;

    private LocalTime timeIn;
    private LocalTime timeOut;

    private Boolean isPass;
    private Integer detectionTime;
    private String remedialAction;
    private String note;

    private Boolean isSignIn;
    private Boolean isSignOut;
}