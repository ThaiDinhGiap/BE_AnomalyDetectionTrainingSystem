package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.EmployeeStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EmployeePlanResponse {

    private Long id;
    private String employeeCode;
    private String fullName;
    private EmployeeStatus status;
    private Long teamId;
    private String teamName;
    private String groupName;

    private Integer tierOrder;
    private String tierName;
    private Integer sortRank;
    private String priorityReason;

    private LocalDate lastTrainedDate;
    private Boolean lastTrainedPassed;

    private Boolean inCurrentPlan;
}