package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.ReportType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class DefectReportDetailResponse {
    Long id;
    ReportType reportType;
    String defectReportDescription;
    String processName;
    LocalDate detectedDate;
    String note;
}
