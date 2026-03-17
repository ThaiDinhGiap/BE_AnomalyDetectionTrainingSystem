package com.sep490.anomaly_training_backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiData {
    // Tiến độ Kế hoạch
    private int planDone;
    private int planTotal;
    private String planProgressPercent;

    // Lịch Huấn luyện hôm nay
    private int todayTrainingCount;
    private int todayFailCount;
    private int todayMissCount;

    // Hồ sơ chờ ký duyệt
    private int pendingSignatureCount;

    // Lỗi phát sinh tháng
    private int monthlyDefectCount;
}
