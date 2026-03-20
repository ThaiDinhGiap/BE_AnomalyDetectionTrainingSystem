package com.sep490.anomaly_training_backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SvTeamBenchmark {
    private Long teamId;
    private String name;       // "Team A"
    private int completion;    // 90 (%)
    private String defects;    // "Cao", "Thấp", "TB"
    private String grade;      // "Tốt", "Khá", "Cần nhắc"
}
