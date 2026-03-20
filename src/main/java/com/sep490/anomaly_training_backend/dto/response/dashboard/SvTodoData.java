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
public class SvTodoData {
    private String passRate;            // "98.5%"
    private String participationRate;   // "100%"
    private int pendingCount;
    private List<SvTodoItem> pendingItems;
}
