package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.TrainingPlanStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class TrainingPlanResponse {
    private Long id;
    private String title;
    private LocalDate monthStart;
    private LocalDate monthEnd;
    private String groupName;
    private TrainingPlanStatus status;
    private Integer currentVersion;
    private String lastRejectReason;

    // Danh sách chi tiết
    private List<TrainingPlanDetailResponse> details;

    // Timeline lịch sử duyệt
    private List<ApprovalLogDto> approvalLogs;
}