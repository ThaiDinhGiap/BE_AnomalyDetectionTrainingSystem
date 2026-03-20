package com.sep490.anomaly_training_backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Item in SkillIssueList — employee needing supervision or re-evaluation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SvWatchlistItem {
    private Long id;
    private String name;       // "Nguyễn Văn A"
    private String empId;      // "EMP-082"
    private String role;       // "Hàn siêu âm bậc 2 • Lắp ráp (P01)"
    private String status;     // "Cần giám sát" / "Fail"
    private String reason;     // "Sản phẩm lỗi vượt mức 2% trong ca"
}
