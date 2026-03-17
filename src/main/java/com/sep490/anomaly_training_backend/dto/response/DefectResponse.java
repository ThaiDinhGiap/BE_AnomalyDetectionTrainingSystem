package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.model.Attachment;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class DefectResponse {
    Long defectId;
    String defectCode;
    String defectDescription;
    Long processId;
    String processName;
    LocalDate detectedDate;
    String note;
    String originCause;
    String outflowCause;
    String causePoint;
    String originMeasures;
    String outflowMeasures;
    String defectType;
    // New fields
    String customer;
    Integer quantity;
    String conclusion;
    ProductResponse product;
    List<String> attachmentUrls;
}
