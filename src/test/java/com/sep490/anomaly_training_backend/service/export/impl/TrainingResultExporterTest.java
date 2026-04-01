package com.sep490.anomaly_training_backend.service.export.impl;

import com.sep490.anomaly_training_backend.enums.ExportEntityType;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.model.TrainingResult;
import com.sep490.anomaly_training_backend.model.TrainingResultDetail;
import com.sep490.anomaly_training_backend.repository.TrainingResultRepository;
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
class TrainingResultExporterTest {

    @Mock
    private TrainingResultRepository repository;

    @InjectMocks
    private TrainingResultExporter exporter;

    private Workbook workbook;
    private ExcelStyleHelper styleHelper;
    private Sheet sheet;
    private TrainingResult result;

    @BeforeEach
    void setUp() {
        workbook = new XSSFWorkbook();
        styleHelper = new ExcelStyleHelper(workbook);
        sheet = workbook.createSheet("Test");

        ProductLine line = new ProductLine();
        line.setName("Line A");
        Team team = new Team();
        team.setName("Team X");

        result = new TrainingResult();
        result.setId(20L);
        result.setFormCode("TR-001");
        result.setTitle("Result 1");
        result.setLine(line);
        result.setTeam(team);
        result.setYear(2025);

        TrainingResultDetail detail = new TrainingResultDetail();
        result.setDetails(List.of(detail));
    }

    @Test
    void getType_ShouldReturnTrainingResult() {
        assertThat(exporter.getType()).isEqualTo(ExportEntityType.TRAINING_RESULT);
    }

    @Test
    void exportSingle_ShouldWriteToSheet() {
        when(repository.findByIdWithDetails(20L)).thenReturn(Optional.of(result));

        exporter.exportSingle(20L, sheet, styleHelper);

        assertThat(sheet.getPhysicalNumberOfRows()).isGreaterThan(2); 
    }

    @Test
    void exportList_ShouldWriteToSheet() {
        when(repository.findByIdIn(anyList())).thenReturn(List.of(result));

        exporter.exportList(sheet, styleHelper, List.of(20L));

        assertThat(sheet.getPhysicalNumberOfRows()).isGreaterThan(1);
    }
}
