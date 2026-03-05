package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TrainingPlanResponse {
    private Long id;
    private String title;
    private String formCode;

    private LocalDate monthStart;
    private LocalDate monthEnd;

    private Long groupId;
    private String groupName;

    private Long lineId;
    private String lineName;

    private ReportStatus status;
    private Integer currentVersion;
    private String note;

    private String createdBy;
    private LocalDateTime createdAt;

    // Flat list - tất cả detail rows
    private List<TrainingPlanDetailResponse> details = new ArrayList<>();

    // Grouped by batchId - FE dùng để hiển thị (mỗi group = 1 row trên FE)
    private List<EmployeePlanGroup> groupedDetails = new ArrayList<>();
}