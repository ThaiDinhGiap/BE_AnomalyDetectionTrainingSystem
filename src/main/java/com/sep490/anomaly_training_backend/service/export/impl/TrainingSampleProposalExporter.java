package com.sep490.anomaly_training_backend.service.export.impl;

import com.sep490.anomaly_training_backend.enums.ExportEntityType;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposal;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposalDetail;
import com.sep490.anomaly_training_backend.repository.TrainingSampleProposalRepository;
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
public class TrainingSampleProposalExporter implements EntityExporter {

    private final TrainingSampleProposalRepository proposalRepository;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public ExportEntityType getType() {
        return ExportEntityType.TRAINING_SAMPLE_PROPOSAL;
    }

    @Override
    public String getSheetName() {
        return "Đề xuất mẫu đào tạo";
    }

    @Override
    public String getFileName(Long id) {
        String date = LocalDate.now().format(FMT);
        return id != null
                ? "De_xuat_mau_" + id + "_" + date + ".xlsx"
                : "DS_De_xuat_mau_" + date + ".xlsx";
    }

    @Override
    @Transactional(readOnly = true)
    public void exportSingle(Long id, Sheet sheet, ExcelStyleHelper styles) {
        TrainingSampleProposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_PROPOSAL_NOT_FOUND));

        int row = 0;
        styles.writeSectionHeader(sheet, row++, "PHIẾU ĐỀ XUẤT CHỈNH SỬA DANH SÁCH MẪU HUẤN LUYỆN");
        row++;
        styles.writeInfoRow(sheet, row++, "Mã phiếu:", proposal.getFormCode());
        styles.writeInfoRow(sheet, row++, "Dây chuyền:", proposal.getProductLine() != null ? proposal.getProductLine().getName() : "");
        styles.writeInfoRow(sheet, row++, "Trạng thái:", proposal.getStatus() != null ? proposal.getStatus().name() : "");
        styles.writeInfoRow(sheet, row++, "Phiên bản:", proposal.getCurrentVersion());
        styles.writeInfoRow(sheet, row++, "Người tạo:", proposal.getCreatedBy());
        row++;

        styles.writeSectionHeader(sheet, row++, "CHI TIẾT");
        styles.writeHeaderRow(sheet, row++,
                "STT", "Loại đề xuất", "Mã mẫu", "Tên danh mục", "Mô tả đào tạo",
                "Công đoạn", "Sản phẩm", "Ghi chú");

        if (proposal.getDetails() != null) {
            int stt = 1;
            for (TrainingSampleProposalDetail d : proposal.getDetails()) {
                Row dataRow = sheet.createRow(row++);
                styles.writeCell(dataRow, 0, stt++);
                styles.writeCell(dataRow, 1, d.getProposalType() != null ? d.getProposalType().name() : "");
                styles.writeCell(dataRow, 2, d.getTrainingSampleCode());
                styles.writeCell(dataRow, 3, d.getCategoryName());
                styles.writeCell(dataRow, 4, d.getTrainingDescription());
                styles.writeCell(dataRow, 5, d.getProcess() != null ? d.getProcess().getName() : "");
                styles.writeCell(dataRow, 6, d.getProduct() != null ? d.getProduct().getName() : "");
                styles.writeCell(dataRow, 7, d.getNote());
            }
        }
        styles.autoSizeColumns(sheet, 8);
    }

    @Override
    @Transactional(readOnly = true)
    public void exportList(Sheet sheet, ExcelStyleHelper styles) {
        List<TrainingSampleProposal> proposals = proposalRepository.findByDeleteFlagFalse();

        styles.writeHeaderRow(sheet, 0,
                "STT", "Mã phiếu", "Dây chuyền", "Trạng thái", "Phiên bản",
                "Số chi tiết", "Người tạo", "Ngày tạo");

        int rowNum = 1;
        int stt = 1;
        for (TrainingSampleProposal p : proposals) {
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
}
