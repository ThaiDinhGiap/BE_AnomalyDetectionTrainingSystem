package com.sep490.anomaly_training_backend.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ScheduleRequest {
    private LocalDate targetMonth;

    private Integer plannedDay;
}