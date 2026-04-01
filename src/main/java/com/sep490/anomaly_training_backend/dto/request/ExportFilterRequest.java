package com.sep490.anomaly_training_backend.dto.request;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Filter criteria for export list endpoints.
 * All fields are optional. When null, the corresponding filter is not applied.
 * Sending an empty body {} exports all records (backward compatible).
 */
@Data
public class ExportFilterRequest {

    /** Filter by created date range (inclusive) */
    private LocalDate fromDate;
    private LocalDate toDate;

    /** Filter by report status */
    private ReportStatus status;

    /** Filter by product line (dây chuyền) */
    private Long productLineId;

    /** Filter by team (tổ) — applicable to TrainingPlan, TrainingResult */
    private Long teamId;

    /** Filter by year — applicable to TrainingResult */
    private Integer year;

    /** Export only specific records by IDs (user-selected from UI) */
    private List<Long> ids;

    /** Keyword search — matches formCode, title */
    private String keyword;
}
