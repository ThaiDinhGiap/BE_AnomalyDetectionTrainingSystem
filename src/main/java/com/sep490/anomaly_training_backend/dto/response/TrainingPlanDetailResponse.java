package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.TrainingPlanDetailStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TrainingPlanDetailResponse {

    @Schema(description = "ID dòng chi tiết (ít dùng để hiển thị, chủ yếu để trace)", example = "1001")
    private Long id;

    // --- Thông tin Nhân viên ---
    @Schema(description = "ID nhân viên", example = "101")
    private Long employeeId;

    @Schema(description = "Tên nhân viên (để hiển thị lên bảng)", example = "Nguyễn Văn A")
    private String employeeName;

    @Schema(description = "Mã nhân viên", example = "V001")
    private String employeeCode;

    // --- Thông tin Công đoạn ---
    @Schema(description = "ID công đoạn", example = "50")
    private Long processId;

    @Schema(description = "Tên công đoạn", example = "Công đoạn OP4")
    private String processName;

    // --- Lịch trình ---
    @Schema(description = "Tháng thuộc cột (Dùng để xác định vị trí vẽ trên bảng)", example = "2016-10-01")
    private LocalDate targetMonth;

    @Schema(description = "Ngày học thực tế (Hiển thị text trong ô)", example = "2016-10-04")
    private LocalDate plannedDate;

    // --- Kết quả ---
    @Schema(description = "Trạng thái (SICK_LEAVE: Tô đỏ/ghi chú, DONE: Tô xanh...)", example = "SICK_LEAVE")
    private TrainingPlanDetailStatus resultStatus;

    @Schema(description = "Ghi chú kèm theo", example = "Nghỉ ốm có phép")
    private String note;
}