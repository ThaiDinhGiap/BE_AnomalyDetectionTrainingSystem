package com.sep490.anomaly_training_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeSelectionResponse {
    @Schema(description = "ID nhân viên", example = "101")
    private Long id;

    @Schema(description = "Mã nhân viên", example = "V12345")
    private String code;

    @Schema(description = "Họ và tên", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "ID của Tổ (Team)", example = "50")
    private Long teamId;

    @Schema(description = "Tên Tổ (Dùng để hiển thị Group By trên bảng)", example = "Team Lắp Ráp 1")
    private String teamName;
}