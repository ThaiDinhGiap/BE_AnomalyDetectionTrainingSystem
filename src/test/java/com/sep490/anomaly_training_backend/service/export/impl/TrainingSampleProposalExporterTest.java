package com.sep490.anomaly_training_backend.service.export.impl;

import com.sep490.anomaly_training_backend.enums.ExportEntityType;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposal;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposalDetail;
import com.sep490.anomaly_training_backend.repository.TrainingSampleProposalRepository;
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
class TrainingSampleProposalExporterTest {

    @Mock
    private TrainingSampleProposalRepository repository;

    @InjectMocks
    private TrainingSampleProposalExporter exporter;

    private Workbook workbook;
    private ExcelStyleHelper styleHelper;
    private Sheet sheet;
    private TrainingSampleProposal proposal;

    @BeforeEach
    void setUp() {
        workbook = new XSSFWorkbook();
        styleHelper = new ExcelStyleHelper(workbook);
        sheet = workbook.createSheet("Test");

        ProductLine line = new ProductLine();
        line.setName("Line A");

        proposal = new TrainingSampleProposal();
        proposal.setId(30L);
        proposal.setFormCode("TSP-001");
        proposal.setProductLine(line);

        TrainingSampleProposalDetail detail = new TrainingSampleProposalDetail();
        proposal.setDetails(List.of(detail));
    }

    @Test
    void getType_ShouldReturnTrainingSampleProposal() {
        assertThat(exporter.getType()).isEqualTo(ExportEntityType.TRAINING_SAMPLE_PROPOSAL);
    }

    @Test
    void exportSingle_ShouldWriteToSheet() {
        when(repository.findById(30L)).thenReturn(Optional.of(proposal));

        exporter.exportSingle(30L, sheet, styleHelper);

        assertThat(sheet.getPhysicalNumberOfRows()).isGreaterThan(2); 
    }

    @Test
    void exportList_ShouldWriteToSheet() {
        when(repository.findByIdIn(anyList())).thenReturn(List.of(proposal));

        exporter.exportList(sheet, styleHelper, List.of(30L));

        assertThat(sheet.getPhysicalNumberOfRows()).isGreaterThan(1);
    }
}
