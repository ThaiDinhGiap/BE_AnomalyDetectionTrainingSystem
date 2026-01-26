package com.sep490.anomaly_training_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class TrainingPlanInitDataResponse {

    @Schema(description = "ID dây chuyền (Group)", example = "10")
    private Long groupId;

    @Schema(description = "Tên dây chuyền", example = "Dây chuyền Máy Vỏ")
    private String groupName;

    @Schema(description = "Danh sách nhân viên khả dụng (Lấy từ các Team thuộc Group)")
    private List<EmployeeSelectionResponse> availableEmployees;

    @Schema(description = "Danh sách công đoạn khả dụng (Dropdown)")
    private List<ProcessSelectionResponse> availableProcesses;
}