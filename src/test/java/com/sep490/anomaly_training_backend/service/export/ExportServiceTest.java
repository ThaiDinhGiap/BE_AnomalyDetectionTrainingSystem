package com.sep490.anomaly_training_backend.service.export;

import com.sep490.anomaly_training_backend.enums.ExportEntityType;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.service.export.EntityExporter;
import com.sep490.anomaly_training_backend.service.export.ExportService;
import com.sep490.anomaly_training_backend.util.helper.ExcelStyleHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock
    private EntityExporter mockExporter;

    private ExportService exportService;

    @BeforeEach
    void setUp() {
        lenient().when(mockExporter.getType()).thenReturn(ExportEntityType.DEFECT_PROPOSAL);
        exportService = new ExportService(List.of(mockExporter));
    }

    @Test
    void exportSingle_ShouldReturnExportResult() {
        when(mockExporter.getSheetName()).thenReturn("Defect");
        when(mockExporter.getFileName(1L)).thenReturn("defect_1.xlsx");
        doNothing().when(mockExporter).exportSingle(eq(1L), any(Sheet.class), any(ExcelStyleHelper.class));

        ExportService.ExportResult result = exportService.exportSingle(ExportEntityType.DEFECT_PROPOSAL, 1L);

        assertThat(result).isNotNull();
        assertThat(result.fileName()).isEqualTo("defect_1.xlsx");
        assertThat(result.data()).isNotEmpty();
        verify(mockExporter).exportSingle(eq(1L), any(Sheet.class), any(ExcelStyleHelper.class));
    }

    @Test
    void exportSingle_WhenExporterNotFound_ShouldThrowException() {
        assertThatThrownBy(() -> exportService.exportSingle(ExportEntityType.TRAINING_PLAN, 1L))
                .isInstanceOf(Exception.class);
    }

    @Test
    void exportSingle_WhenExceptionThrown_ShouldThrowAppException() {
        when(mockExporter.getSheetName()).thenReturn("Defect");
        doThrow(new RuntimeException("POI error")).when(mockExporter).exportSingle(any(), any(), any());

        assertThatThrownBy(() -> exportService.exportSingle(ExportEntityType.DEFECT_PROPOSAL, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("POI error");
    }

    @Test
    void exportList_ShouldReturnExportResult() {
        when(mockExporter.getSheetName()).thenReturn("Defects");
        when(mockExporter.getFileName(null)).thenReturn("defects_list.xlsx");
        doNothing().when(mockExporter).exportList(any(Sheet.class), any(ExcelStyleHelper.class), any());

        ExportService.ExportResult result = exportService.exportList(ExportEntityType.DEFECT_PROPOSAL, List.of(1L, 2L));

        assertThat(result).isNotNull();
        assertThat(result.fileName()).isEqualTo("defects_list.xlsx");
        assertThat(result.data()).isNotEmpty();
        verify(mockExporter).exportList(any(Sheet.class), any(ExcelStyleHelper.class), eq(List.of(1L, 2L)));
    }
}
