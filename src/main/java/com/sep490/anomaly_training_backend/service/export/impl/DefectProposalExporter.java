package com.sep490.anomaly_training_backend.service.export.impl;

import com.sep490.anomaly_training_backend.dto.request.ExportFilterRequest;
import com.sep490.anomaly_training_backend.enums.ExportEntityType;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.DefectProposal;
import com.sep490.anomaly_training_backend.model.DefectProposalDetail;
import com.sep490.anomaly_training_backend.repository.DefectProposalRepository;
import com.sep490.anomaly_training_backend.service.export.EntityExporter;
import com.sep490.anomaly_training_backend.service.export.ExcelStyleHelper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefectProposalExporter implements EntityExporter {

    private final DefectProposalRepository defectProposalRepository;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public ExportEntityType getType() {
        return ExportEntityType.DEFECT_PROPOSAL;
    }

    @Override
    public String getSheetName() {
        return "Đề xuất lỗi";
    }

    @Override
    public String getFileName(Long id) {
        String date = LocalDate.now().format(FMT);
        return id != null
                ? "De_xuat_loi_" + id + "_" + date + ".xlsx"
                : "DS_De_xuat_loi_" + date + ".xlsx";
    }

    @Override
    @Transactional(readOnly = true)
    public void exportSingle(Long id, Sheet sheet, ExcelStyleHelper styles) {
        DefectProposal proposal = defectProposalRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEFECT_PROPOSAL_NOT_FOUND));

        // Header info
        int row = 0;
        styles.writeSectionHeader(sheet, row++, "PHIẾU ĐỀ XUẤT CHỈNH SỬA DANH SÁCH LỖI QUÁ KHỨ");
        row++;
        styles.writeInfoRow(sheet, row++, "Mã phiếu:", proposal.getFormCode());
        styles.writeInfoRow(sheet, row++, "Dây chuyền:", proposal.getProductLine() != null ? proposal.getProductLine().getName() : "");
        styles.writeInfoRow(sheet, row++, "Trạng thái:", proposal.getStatus() != null ? proposal.getStatus().name() : "");
        styles.writeInfoRow(sheet, row++, "Phiên bản:", proposal.getCurrentVersion());
        styles.writeInfoRow(sheet, row++, "Người tạo:", proposal.getCreatedBy());
        row++;

        // Detail table
        styles.writeSectionHeader(sheet, row++, "CHI TIẾT");
        writeDetailHeaders(sheet, row++, styles);
        if (proposal.getDetails() != null) {
            int stt = 1;
            for (DefectProposalDetail d : proposal.getDetails()) {
                Row dataRow = sheet.createRow(row++);
                writeDetailRow(dataRow, stt++, d, styles);
            }
        }
        styles.autoSizeColumns(sheet, 13);
    }

    @Override
    @Transactional(readOnly = true)
    public void exportList(Sheet sheet, ExcelStyleHelper styles, ExportFilterRequest filter) {
        LocalDateTime fromDateTime = filter.getFromDate() != null
                ? filter.getFromDate().atStartOfDay() : null;
        LocalDateTime toDateTime = filter.getToDate() != null
                ? filter.getToDate().atTime(LocalTime.MAX) : null;

        List<DefectProposal> proposals = defectProposalRepository.findByExportFilters(
                filter.getStatus(),
                filter.getProductLineId(),
                fromDateTime,
                toDateTime,
                filter.getIds(),
                filter.getKeyword());

        styles.writeHeaderRow(sheet, 0,
                "STT", "Mã phiếu", "Dây chuyền", "Trạng thái", "Phiên bản",
                "Số chi tiết", "Người tạo", "Ngày tạo");

        int rowNum = 1;
        int stt = 1;
        for (DefectProposal p : proposals) {
            Row row = sheet.createRow(rowNum++);
            styles.writeCell(row, 0, stt++);
            styles.writeCell(row, 1, p.getFormCode());
            styles.writeCell(row, 2, p.getProductLine() != null ? p.getProductLine().getName() : "");
            styles.writeCell(row, 3, p.getStatus() != null ? p.getStatus().name() : "");
            styles.writeCell(row, 4, p.getCurrentVersion());
            styles.writeCell(row, 5, p.getDetails() != null ? p.getDetails().size() : 0);
            styles.writeCell(row, 6, p.getCreatedBy());
            styles.writeCell(row, 7, p.getCreatedAt() != null ? p.getCreatedAt().toLocalDate() : null);
        }
        styles.autoSizeColumns(sheet, 8);
    }

    private void writeDetailHeaders(Sheet sheet, int rowNum, ExcelStyleHelper styles) {
        styles.writeHeaderRow(sheet, rowNum,
                "STT", "Loại đề xuất", "Mô tả lỗi", "Công đoạn", "Ngày phát hiện",
                "Phân loại lỗi", "Nguyên nhân gốc", "Nguyên nhân lọt",
                "Biện pháp gốc", "Biện pháp lọt", "Khách hàng", "Số lượng", "Ghi chú");
    }

    private void writeDetailRow(Row row, int stt, DefectProposalDetail d, ExcelStyleHelper styles) {
        styles.writeCell(row, 0, stt);
        styles.writeCell(row, 1, d.getProposalType() != null ? d.getProposalType().name() : "");
        styles.writeCell(row, 2, d.getDefectDescription());
        styles.writeCell(row, 3, d.getProcess() != null ? d.getProcess().getName() : "");
        styles.writeCell(row, 4, d.getDetectedDate());
        styles.writeCell(row, 5, d.getDefectType() != null ? d.getDefectType().name() : "");
        styles.writeCell(row, 6, d.getOriginCause());
        styles.writeCell(row, 7, d.getOutflowCause());
        styles.writeCell(row, 8, d.getOriginMeasures());
        styles.writeCell(row, 9, d.getOutflowMeasures());
        styles.writeCell(row, 10, d.getCustomer());
        styles.writeCell(row, 11, d.getQuantity());
        styles.writeCell(row, 12, d.getNote());
    }
}
