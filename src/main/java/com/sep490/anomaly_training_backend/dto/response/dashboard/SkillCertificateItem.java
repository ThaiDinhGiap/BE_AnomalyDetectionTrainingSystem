package com.sep490.anomaly_training_backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillCertificateItem {
    private String process;

    private int valid;
    private List<String> validNames;

    private int expiring;
    private List<String> expiringNames;

    private int revoked;
    private List<String> revokedNames;
}
