package com.sep490.anomaly_training_backend.service.export.impl;

import com.sep490.anomaly_training_backend.enums.ExportEntityType;
import com.sep490.anomaly_training_backend.model.DefectProposal;
import com.sep490.anomaly_training_backend.model.DefectProposalDetail;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.repository.DefectProposalRepository;
import com.sep490.anomaly_training_backend.util.helper.ExcelStyleHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefectProposalExporterTest {

    @Mock
    private DefectProposalRepository repository;

    @InjectMocks
    private DefectProposalExporter exporter;

    private Workbook workbook;
    private ExcelStyleHelper styleHelper;
    private Sheet sheet;

    private DefectProposal proposal;

    @BeforeEach
    void setUp() {
        workbook = new XSSFWorkbook();
        styleHelper = new ExcelStyleHelper(workbook);
        sheet = workbook.createSheet("Test");

        ProductLine line = new ProductLine();
        line.setName("Line A");

        proposal = new DefectProposal();
        proposal.setId(1L);
        proposal.setFormCode("DP-001");
        proposal.setProductLine(line);
        proposal.setCurrentVersion(1);

        DefectProposalDetail detail = new DefectProposalDetail();
        detail.setDefectDescription("Error 1");
        proposal.setDetails(List.of(detail));
    }

    @Test
    void getType_ShouldReturnDefectProposal() {
        assertThat(exporter.getType()).isEqualTo(ExportEntityType.DEFECT_PROPOSAL);
    }

    @Test
    void exportSingle_ShouldWriteToSheet() {
        when(repository.findById(1L)).thenReturn(Optional.of(proposal));

        exporter.exportSingle(1L, sheet, styleHelper);
        // Wait! The repository parameter is ID type.

        assertThat(sheet.getPhysicalNumberOfRows()).isGreaterThan(2); 
    }

    @Test
    void exportList_ShouldWriteToSheet() {
        when(repository.findByIdIn(anyList())).thenReturn(List.of(proposal));

        exporter.exportList(sheet, styleHelper, List.of(1L));

        assertThat(sheet.getPhysicalNumberOfRows()).isGreaterThan(1);
    }
}
