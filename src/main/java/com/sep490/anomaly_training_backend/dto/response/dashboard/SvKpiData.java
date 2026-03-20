package com.sep490.anomaly_training_backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SV KPI dashboard cards:
 * - Tỷ lệ đạt (passRate)
 * - Lỗi phát sinh (defectCount)
 * - Độ phủ đào tạo (trainingCoverage)
 * - Chờ phê duyệt (pendingApprovalCount)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SvKpiData {
    private String passRate;               // "66%"
    private String passRateSub;            // "+0.4% so với tháng trước"
    private int defectCount;               // 8
    private String defectSub;              // "-18.7% so với tháng trước"
    private String trainingCoverage;       // "92.4%"
    private String coverageSub;            // "+0.4% so với quý trước"
    private int pendingApprovalCount;      // 31
    private String pendingSub;             // "3 báo cáo lỗi quá hạn SLA (24h)"
}
