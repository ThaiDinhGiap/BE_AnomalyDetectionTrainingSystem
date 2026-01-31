package com.sep490.anomaly_training_backend.dto.request;

import com.sep490.anomaly_training_backend.enums.ReportType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class CreateDefectReportDetailRequest {
    Long defectId;
    ReportType reportType;
    String defectDescription;
    Long processId;
    LocalDate detectedDate;
    String note;
}
