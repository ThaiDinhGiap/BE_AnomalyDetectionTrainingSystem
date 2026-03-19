package com.sep490.anomaly_training_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class EmployeeSkillCertificateResponse {

    // ── Thông tin nhân viên ──────────────────────────────────────────────────
    private Long employeeId;
    private String employeeCode;
    private String fullName;

    // Số chứng chỉ đang hoạt động (VALID, chưa hết hạn)
    private int activeCertCount;
    // Tỷ lệ hoạt động = activeCertCount / totalCertCount * 100
    private double activeRate;

    // ── Chứng chỉ từng công đoạn ─────────────────────────────────────────────
    private List<ProcessCertDetail> processDetails;

    // ── Các buổi huấn luyện trong kế hoạch này ──────────────────────────────
    private List<PlannedSessionDto> plannedSessions;

    // ════════════════════════════════════════════════════════════════════════
    // Inner DTOs
    // ════════════════════════════════════════════════════════════════════════

    @Data
    @Builder
    public static class ProcessCertDetail {
        private Long processId;
        private String processCode;
        private String processName;
        private String jtCode;             // Mã JT (standardTimeJt)

        // Phân loại: 1-4 tương ứng C1-C4 của ProcessClassification
        private Integer classification;
        private String classificationLabel; // "Loại 1" → "Loại 4"
        private String classificationDesc;  // Mô tả chi tiết từ legend

        // Trạng thái chứng chỉ: VALID, PENDING_REVIEW, REVOKED, null=không có
        private String certStatus;
        private LocalDate certifiedDate;
        private LocalDate expiryDate;

        // Lịch sử 6 lần gần nhất: true=Pass, false=Fail, null=không có data
        // Thứ tự: index 0 = gần nhất
        private List<Boolean> recentHistory;

        // Thống kê toàn bộ (không giới hạn trong kế hoạch hiện tại)
        private int passCount;
        private int failCount;
        private int totalCount;

        // Kết quả lần gần nhất
        private Boolean latestResult; // true=Pass, false=Fail, null=chưa có
    }

    @Data
    @Builder
    public static class PlannedSessionDto {
        private Long detailId;
        private LocalDate plannedDate;
        private LocalDate actualDate;

        private Long processId;
        private String processCode;
        private String processName;

        private String sampleCode;
        private String trainingTopic;
        private String note;

        // Người xác nhận (PRO out hoặc FI out đã ký)
        private String confirmerName;

        // "Dự kiến" nếu chưa có kết quả, "Đạt" nếu pass, "Không đạt" nếu fail
        private String evaluation;
        private Boolean isPass;
    }
}