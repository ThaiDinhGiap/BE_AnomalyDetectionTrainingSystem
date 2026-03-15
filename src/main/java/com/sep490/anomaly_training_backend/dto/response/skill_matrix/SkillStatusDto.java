package com.sep490.anomaly_training_backend.dto.response.skill_matrix;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class SkillStatusDto {
    private String status; // e.g., "PASS", "WARNING", "EXPIRED", "PENDING", "NONE"
    private LocalDate expiryDate;
}
