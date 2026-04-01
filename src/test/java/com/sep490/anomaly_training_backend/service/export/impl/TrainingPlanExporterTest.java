package com.sep490.anomaly_training_backend.service.export.impl;

import com.sep490.anomaly_training_backend.enums.ExportEntityType;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.TrainingPlanDetail;
import com.sep490.anomaly_training_backend.repository.TrainingPlanRepository;
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
class TrainingPlanExporterTest {

    @Mock
    private TrainingPlanRepository repository;

    @InjectMocks
    private TrainingPlanExporter exporter;

    private Workbook workbook;
    private ExcelStyleHelper styleHelper;
    private Sheet sheet;
    private TrainingPlan plan;

    @BeforeEach
    void setUp() {
        workbook = new XSSFWorkbook();
        styleHelper = new ExcelStyleHelper(workbook);
        sheet = workbook.createSheet("Test");

        ProductLine line = new ProductLine();
        line.setName("Line A");
        Team team = new Team();
        team.setName("Team X");

        plan = new TrainingPlan();
        plan.setId(10L);
        plan.setFormCode("TP-001");
        plan.setTitle("Plan 1");
        plan.setLine(line);
        plan.setTeam(team);
        plan.setStartDate(LocalDate.now());
        plan.setEndDate(LocalDate.now().plusDays(5));

        TrainingPlanDetail detail = new TrainingPlanDetail();
        plan.setDetails(List.of(detail));
    }

    @Test
    void getType_ShouldReturnTrainingPlan() {
        assertThat(exporter.getType()).isEqualTo(ExportEntityType.TRAINING_PLAN);
    }

    @Test
    void exportSingle_ShouldWriteToSheet() {
        when(repository.findById(10L)).thenReturn(Optional.of(plan));

        exporter.exportSingle(10L, sheet, styleHelper);

        assertThat(sheet.getPhysicalNumberOfRows()).isGreaterThan(2);
    }

    @Test
    void exportList_ShouldWriteToSheet() {
        when(repository.findByIdIn(anyList())).thenReturn(List.of(plan));

        exporter.exportList(sheet, styleHelper, List.of(10L));

        assertThat(sheet.getPhysicalNumberOfRows()).isGreaterThan(1);
    }
}
