package com.sep490.anomaly_training_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeTrainingHistoryResponse {
    private Long employeeId;
    private String employeeCode;
    private String fullName;
    private int totalRecords;
    private List<TrainingHistoryItem> histories;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainingHistoryItem {
        private Long detailId;

        // Kế hoạch
        private String planTitle;
        private String teamLeadName;

        // Thời gian
        private LocalDate actualDate;
        private LocalTime timeIn;

        // Công đoạn
        private Long processId;
        private String processCode;
        private String processName;

        // Hạng mục kiểm tra
        private String trainingTopic;

        // Ghi chú
        private String note;

        // Kết quả
        private Boolean isPass;
    }
}
