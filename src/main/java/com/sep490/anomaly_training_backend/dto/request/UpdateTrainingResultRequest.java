package com.sep490.anomaly_training_backend.dto.request;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
import lombok.Data;
import java.util.List;

@Data
public class UpdateTrainingResultRequest {
    private Long id;
    private String title;
    private String note;
    private ReportStatus status;

    private List<UpdateResultDetailRequest> details;
}