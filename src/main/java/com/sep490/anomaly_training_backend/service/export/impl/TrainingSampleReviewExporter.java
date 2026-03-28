package com.sep490.anomaly_training_backend.service.export.impl;

import com.sep490.anomaly_training_backend.enums.ExportEntityType;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.TrainingSampleReview;
import com.sep490.anomaly_training_backend.repository.TrainingSampleReviewRepository;
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
public class TrainingSampleReviewExporter implements EntityExporter {

    private final TrainingSampleReviewRepository reviewRepository;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public ExportEntityType getType() {
        return ExportEntityType.TRAINING_SAMPLE_REVIEW;
    }

    @Override
    public String getSheetName() {
        return "Rà soát mẫu đào tạo";
    }

    @Override
    public String getFileName(Long id) {
        String date = LocalDate.now().format(FMT);
        return id != null
                ? "Ra_soat_mau_" + id + "_" + date + ".xlsx"
                : "DS_Ra_soat_mau_" + date + ".xlsx";
    }

    @Override
    @Transactional(readOnly = true)
    public void exportSingle(Long id, Sheet sheet, ExcelStyleHelper styles) {
        TrainingSampleReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_REPORT_NOT_FOUND));

        int row = 0;
        styles.writeSectionHeader(sheet, row++, "PHIẾU RÀ SOÁT MẪU ĐÀO TẠO");
        row++;
        styles.writeInfoRow(sheet, row++, "Dây chuyền:", review.getProductLine() != null ? review.getProductLine().getName() : "");
        styles.writeInfoRow(sheet, row++, "Ngày rà soát:", review.getReviewDate());
        styles.writeInfoRow(sheet, row++, "Ngày bắt đầu:", review.getStartDate());
        styles.writeInfoRow(sheet, row++, "Hạn chót:", review.getDueDate());
        styles.writeInfoRow(sheet, row++, "Ngày hoàn thành:", review.getCompletedDate());
        styles.writeInfoRow(sheet, row++, "Trạng thái:", review.getStatus() != null ? review.getStatus().name() : "");
        styles.writeInfoRow(sheet, row++, "Người rà soát:", review.getReviewedBy() != null ? review.getReviewedBy().getFullName() : "");
        styles.writeInfoRow(sheet, row++, "Người xác nhận:", review.getConfirmedBy() != null ? review.getConfirmedBy().getFullName() : "");
        styles.writeInfoRow(sheet, row++, "Phiên bản:", review.getCurrentVersion());
        styles.writeInfoRow(sheet, row++, "Người tạo:", review.getCreatedBy());
        row++;

        // Write snapshot info if available
        if (review.getSampleSnapshot() != null && !review.getSampleSnapshot().isEmpty()) {
            styles.writeSectionHeader(sheet, row++, "DỮ LIỆU SNAPSHOT");
            Row snapshotRow = sheet.createRow(row++);
            styles.writeCell(snapshotRow, 0, "Snapshot JSON đã được lưu (xem chi tiết trong hệ thống)");
        }

        styles.autoSizeColumns(sheet, 2);
    }

    @Override
    @Transactional(readOnly = true)
    public void exportList(Sheet sheet, ExcelStyleHelper styles) {
        List<TrainingSampleReview> reviews = reviewRepository.findByDeleteFlagFalse();

        styles.writeHeaderRow(sheet, 0,
                "STT", "Dây chuyền", "Ngày rà soát", "Hạn chót",
                "Ngày hoàn thành", "Trạng thái", "Người rà soát",
                "Người xác nhận", "Người tạo");

        int rowNum = 1;
        int stt = 1;
        for (TrainingSampleReview r : reviews) {
            Row row = sheet.createRow(rowNum++);
            styles.writeCell(row, 0, stt++);
            styles.writeCell(row, 1, r.getProductLine() != null ? r.getProductLine().getName() : "");
            styles.writeCell(row, 2, r.getReviewDate());
            styles.writeCell(row, 3, r.getDueDate());
            styles.writeCell(row, 4, r.getCompletedDate());
            styles.writeCell(row, 5, r.getStatus() != null ? r.getStatus().name() : "");
            styles.writeCell(row, 6, r.getReviewedBy() != null ? r.getReviewedBy().getFullName() : "");
            styles.writeCell(row, 7, r.getConfirmedBy() != null ? r.getConfirmedBy().getFullName() : "");
            styles.writeCell(row, 8, r.getCreatedBy());
        }
        styles.autoSizeColumns(sheet, 9);
    }
}
