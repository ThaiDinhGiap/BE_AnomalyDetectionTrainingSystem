// src/main/java/com/sep490/anomaly_training_backend/dto/request/TrainingPlanCreateRequest.java
package com.sep490.anomaly_training_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TrainingPlanCreateRequest {

    @NotNull(message = "Tên kế hoạch không được để trống")
    private String title;

    @NotNull(message = "Vui lòng chọn Dây chuyền (Group)")
    private Long groupId;

    @NotNull(message = "Vui lòng chọn Product Line")
    private Long lineId;

    @NotNull(message = "Tháng bắt đầu không được để trống")
    private LocalDate monthStart;

    @NotNull(message = "Tháng kết thúc không được để trống")
    private LocalDate monthEnd;
}