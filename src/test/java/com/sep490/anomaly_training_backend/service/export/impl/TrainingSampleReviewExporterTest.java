package com.sep490.anomaly_training_backend.service.export.impl;

import com.sep490.anomaly_training_backend.enums.ExportEntityType;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.TrainingSampleReview;
import com.sep490.anomaly_training_backend.repository.TrainingSampleReviewRepository;
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
class TrainingSampleReviewExporterTest {

    @Mock
    private TrainingSampleReviewRepository repository;

    @InjectMocks
    private TrainingSampleReviewExporter exporter;

    private Workbook workbook;
    private ExcelStyleHelper styleHelper;
    private Sheet sheet;
    private TrainingSampleReview review;

    @BeforeEach
    void setUp() {
        workbook = new XSSFWorkbook();
        styleHelper = new ExcelStyleHelper(workbook);
        sheet = workbook.createSheet("Test");

        ProductLine line = new ProductLine();
        line.setName("Line A");

        review = new TrainingSampleReview();
        review.setId(40L);
        review.setProductLine(line);
        review.setReviewDate(LocalDate.now());
    }

    @Test
    void getType_ShouldReturnTrainingSampleReview() {
        assertThat(exporter.getType()).isEqualTo(ExportEntityType.TRAINING_SAMPLE_REVIEW);
    }

    @Test
    void exportSingle_ShouldWriteToSheet() {
        when(repository.findById(40L)).thenReturn(Optional.of(review));

        exporter.exportSingle(40L, sheet, styleHelper);

        assertThat(sheet.getPhysicalNumberOfRows()).isGreaterThan(2); 
    }

    @Test
    void exportList_ShouldWriteToSheet() {
        when(repository.findByIdIn(anyList())).thenReturn(List.of(review));

        exporter.exportList(sheet, styleHelper, List.of(40L));

        assertThat(sheet.getPhysicalNumberOfRows()).isGreaterThan(1);
    }
}
