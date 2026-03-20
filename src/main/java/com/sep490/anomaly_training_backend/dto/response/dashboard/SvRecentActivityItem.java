package com.sep490.anomaly_training_backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Item in RecentActivity — recent evaluation & certification events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SvRecentActivityItem {
    private Long id;
    private String name;       // "Hoàng Văn E"
    private String action;     // "Đánh giá Không đạt"
    private String model;      // "HL Lắp ráp P01"
    private String time;       // "2 giờ trước"
    private String status;     // "success" / "fail"
}
