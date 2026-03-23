package com.sep490.anomaly_training_backend.dto.response.dashboard;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MngOrgStats {
    int totalGroups;
    int totalLines;
    int totalTeams;
    int totalEmployees;
}
