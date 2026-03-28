package com.sep490.anomaly_training_backend.service.export.impl;

import com.sep490.anomaly_training_backend.enums.ExportEntityType;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.TrainingResult;
import com.sep490.anomaly_training_backend.model.TrainingResultDetail;
import com.sep490.anomaly_training_backend.repository.TrainingResultRepository;
import com.sep490.anomaly_training_backend.service.export.EntityExporter;
import com.sep490.anomaly_training_backend.service.export.ExcelStyleHelper;
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
public class TrainingResultExporter implements EntityExporter {

    private final TrainingResultRepository trainingResultRepository;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public ExportEntityType getType() {
        return ExportEntityType.TRAINING_RESULT;
    }

    @Override
    public String getSheetName() {
        return "Kết quả đào tạo";
    }

    @Override
    public String getFileName(Long id) {
        String date = LocalDate.now().format(FMT);
        return id != null
                ? "Ket_qua_dao_tao_" + id + "_" + date + ".xlsx"
                : "DS_Ket_qua_dao_tao_" + date + ".xlsx";
    }

    @Override
    @Transactional(readOnly = true)
    public void exportSingle(Long id, Sheet sheet, ExcelStyleHelper styles) {
        TrainingResult result = trainingResultRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_RESULT_NOT_FOUND));

        int row = 0;
        styles.writeSectionHeader(sheet, row++, "PHIẾU KẾT QUẢ ĐÀO TẠO");
        row++;
        styles.writeInfoRow(sheet, row++, "Mã phiếu:", result.getFormCode());
        styles.writeInfoRow(sheet, row++, "Tiêu đề:", result.getTitle());
        styles.writeInfoRow(sheet, row++, "Năm:", result.getYear());
        styles.writeInfoRow(sheet, row++, "Tổ:", result.getTeam() != null ? result.getTeam().getName() : "");
        styles.writeInfoRow(sheet, row++, "Dây chuyền:", result.getLine() != null ? result.getLine().getName() : "");
        styles.writeInfoRow(sheet, row++, "Trạng thái:", result.getStatus() != null ? result.getStatus().name() : "");
        styles.writeInfoRow(sheet, row++, "Phiên bản:", result.getCurrentVersion());
        styles.writeInfoRow(sheet, row++, "Người tạo:", result.getCreatedBy());
        styles.writeInfoRow(sheet, row++, "Ghi chú:", result.getNote());
        row++;

        styles.writeSectionHeader(sheet, row++, "CHI TIẾT KẾT QUẢ");
        styles.writeHeaderRow(sheet, row++,
                "STT", "Mã NV", "Tên nhân viên", "Công đoạn", "Mẫu đào tạo",
                "Chủ đề", "Ngày dự kiến", "Ngày thực tế",
                "Giờ vào", "Giờ bắt đầu", "Giờ ra",
                "Đạt/Không đạt", "Trạng thái", "Huấn luyện lại", "Ghi chú");

        if (result.getDetails() != null) {
            int stt = 1;
            for (TrainingResultDetail d : result.getDetails()) {
                Row dataRow = sheet.createRow(row++);
                styles.writeCell(dataRow, 0, stt++);
                styles.writeCell(dataRow, 1, d.getEmployee() != null ? d.getEmployee().getEmployeeCode() : "");
                styles.writeCell(dataRow, 2, d.getEmployee() != null ? d.getEmployee().getFullName() : "");
                styles.writeCell(dataRow, 3, d.getProcess() != null ? d.getProcess().getName() : "");
                styles.writeCell(dataRow, 4, d.getSampleCode());
                styles.writeCell(dataRow, 5, d.getTrainingTopic());
                styles.writeCell(dataRow, 6, d.getPlannedDate());
                styles.writeCell(dataRow, 7, d.getActualDate());
                styles.writeCell(dataRow, 8, d.getTimeIn() != null ? d.getTimeIn().format(TIME_FMT) : "");
                styles.writeCell(dataRow, 9, d.getTimeStartOp() != null ? d.getTimeStartOp().format(TIME_FMT) : "");
                styles.writeCell(dataRow, 10, d.getTimeOut() != null ? d.getTimeOut().format(TIME_FMT) : "");
                styles.writeCell(dataRow, 11, d.getIsPass() != null ? (d.getIsPass() ? "Đạt" : "Không đạt") : "Chưa đánh giá");
                styles.writeCell(dataRow, 12, d.getStatus() != null ? d.getStatus().name() : "");
                styles.writeCell(dataRow, 13, d.getIsRetrained() != null && d.getIsRetrained() ? "Có" : "Không");
                styles.writeCell(dataRow, 14, d.getNote());
            }
        }
        styles.autoSizeColumns(sheet, 15);
    }

    @Override
    @Transactional(readOnly = true)
    public void exportList(Sheet sheet, ExcelStyleHelper styles) {
        List<TrainingResult> results = trainingResultRepository.findByDeleteFlagFalse();

        styles.writeHeaderRow(sheet, 0,
                "STT", "Mã phiếu", "Tiêu đề", "Năm", "Tổ", "Dây chuyền",
                "Trạng thái", "Số chi tiết", "Người tạo");

        int rowNum = 1;
        int stt = 1;
        for (TrainingResult r : results) {
            Row row = sheet.createRow(rowNum++);
            styles.writeCell(row, 0, stt++);
            styles.writeCell(row, 1, r.getFormCode());
            styles.writeCell(row, 2, r.getTitle());
            styles.writeCell(row, 3, r.getYear());
            styles.writeCell(row, 4, r.getTeam() != null ? r.getTeam().getName() : "");
            styles.writeCell(row, 5, r.getLine() != null ? r.getLine().getName() : "");
            styles.writeCell(row, 6, r.getStatus() != null ? r.getStatus().name() : "");
            styles.writeCell(row, 7, r.getDetails() != null ? r.getDetails().size() : 0);
            styles.writeCell(row, 8, r.getCreatedBy());
        }
        styles.autoSizeColumns(sheet, 9);
    }
}
