package com.sep490.anomaly_training_backend.service.export.impl;

import com.sep490.anomaly_training_backend.enums.ExportEntityType;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.TrainingPlanDetail;
import com.sep490.anomaly_training_backend.repository.TrainingPlanRepository;
import com.sep490.anomaly_training_backend.service.export.EntityExporter;
import com.sep490.anomaly_training_backend.util.helper.ExcelStyleHelper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingPlanExporter implements EntityExporter {

    private final TrainingPlanRepository trainingPlanRepository;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public ExportEntityType getType() {
        return ExportEntityType.TRAINING_PLAN;
    }

    @Override
    public String getSheetName() {
        return "Kế hoạch đào tạo";
    }

    @Override
    public String getFileName(Long id) {
        String date = LocalDate.now().format(FMT);
        return id != null
                ? "Ke_hoach_dao_tao_" + id + "_" + date + ".xlsx"
                : "DS_Ke_hoach_dao_tao_" + date + ".xlsx";
    }

    @Override
    @Transactional(readOnly = true)
    public void exportSingle(Long id, Sheet sheet, ExcelStyleHelper styles) {
        TrainingPlan plan = trainingPlanRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));

        int row = 0;
        styles.writeSectionHeader(sheet, row++, "KẾ HOẠCH ĐÀO TẠO");
        row++;
        styles.writeInfoRow(sheet, row++, "Mã phiếu:", plan.getFormCode());
        styles.writeInfoRow(sheet, row++, "Tiêu đề:", plan.getTitle());
        styles.writeInfoRow(sheet, row++, "Tổ:", plan.getTeam() != null ? plan.getTeam().getName() : "");
        styles.writeInfoRow(sheet, row++, "Dây chuyền:", plan.getLine() != null ? plan.getLine().getName() : "");
        styles.writeInfoRow(sheet, row++, "Từ ngày:", plan.getStartDate());
        styles.writeInfoRow(sheet, row++, "Đến ngày:", plan.getEndDate());
        styles.writeInfoRow(sheet, row++, "Trạng thái:", plan.getStatus() != null ? plan.getStatus().name() : "");
        styles.writeInfoRow(sheet, row++, "Min/Max mỗi ngày:",
                (plan.getMinTrainingPerDay() != null ? plan.getMinTrainingPerDay() : "") + " / "
                        + (plan.getMaxTrainingPerDay() != null ? plan.getMaxTrainingPerDay() : ""));
        styles.writeInfoRow(sheet, row++, "Phiên bản:", plan.getCurrentVersion());
        styles.writeInfoRow(sheet, row++, "Người tạo:", plan.getCreatedBy());
        styles.writeInfoRow(sheet, row++, "Ghi chú:", plan.getNote());
        row++;

        styles.writeSectionHeader(sheet, row++, "CHI TIẾT LỊCH ĐÀO TẠO");
        styles.writeHeaderRow(sheet, row++,
                "STT", "Mã NV", "Tên nhân viên", "Batch ID", "Tháng mục tiêu",
                "Ngày dự kiến", "Ngày thực tế", "Trạng thái", "Ghi chú");

        if (plan.getDetails() != null) {
            int stt = 1;
            for (TrainingPlanDetail d : plan.getDetails()) {
                Row dataRow = sheet.createRow(row++);
                styles.writeCell(dataRow, 0, stt++);
                styles.writeCell(dataRow, 1, d.getEmployee() != null ? d.getEmployee().getEmployeeCode() : "");
                styles.writeCell(dataRow, 2, d.getEmployee() != null ? d.getEmployee().getFullName() : "");
                styles.writeCell(dataRow, 3, d.getBatchId());
                styles.writeCell(dataRow, 4, d.getTargetMonth());
                styles.writeCell(dataRow, 5, d.getPlannedDate());
                styles.writeCell(dataRow, 6, d.getActualDate());
                styles.writeCell(dataRow, 7, d.getStatus() != null ? d.getStatus().name() : "");
                styles.writeCell(dataRow, 8, d.getNote());
            }
        }
        styles.autoSizeColumns(sheet, 9);
    }

    @Override
    @Transactional(readOnly = true)
    public void exportList(Sheet sheet, ExcelStyleHelper styles, List<Long> ids) {
        List<TrainingPlan> plans = trainingPlanRepository.findByIdIn(ids);

        styles.writeHeaderRow(sheet, 0,
                "STT", "Mã phiếu", "Tiêu đề", "Tổ", "Dây chuyền",
                "Ngày bắt đầu", "Ngày kết thúc", "Trạng thái", "Số lượt huấn luyện", "Người tạo");

        int rowNum = 1;
        int stt = 1;
        for (TrainingPlan p : plans) {
            Row row = sheet.createRow(rowNum++);
            styles.writeCell(row, 0, stt++);
            styles.writeCell(row, 1, p.getFormCode());
            styles.writeCell(row, 2, p.getTitle());
            styles.writeCell(row, 3, p.getTeam() != null ? p.getTeam().getName() : "");
            styles.writeCell(row, 4, p.getLine() != null ? p.getLine().getName() : "");
            styles.writeCell(row, 5, p.getStartDate());
            styles.writeCell(row, 6, p.getEndDate());
            styles.writeCell(row, 7, p.getStatus() != null ? p.getStatus().name() : "");
            styles.writeCell(row, 8, p.getDetails() != null ? p.getDetails().size() : 0);
            styles.writeCell(row, 9, p.getCreatedBy());
        }
        styles.autoSizeColumns(sheet, 10);
    }
}

