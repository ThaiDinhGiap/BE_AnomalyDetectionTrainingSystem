package com.sep490.anomaly_training_backend.service.export;

import com.sep490.anomaly_training_backend.enums.ExportEntityType;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Orchestrator service for Excel export.
 * Auto-discovers all EntityExporter beans via Spring injection.
 */
@Service
@Slf4j
public class ExportService {

    private final Map<ExportEntityType, EntityExporter> exporterMap;

    public ExportService(List<EntityExporter> exporters) {
        this.exporterMap = exporters.stream()
                .collect(Collectors.toMap(EntityExporter::getType, e -> e));
        log.info("[ExportService] Registered {} entity exporters: {}",
                exporterMap.size(), exporterMap.keySet());
    }

    /**
     * Export a single entity to Excel bytes.
     */
    public ExportResult exportSingle(ExportEntityType type, Long id) {
        EntityExporter exporter = getExporter(type);

        try (Workbook workbook = new XSSFWorkbook()) {
            ExcelStyleHelper styles = new ExcelStyleHelper(workbook);
            Sheet sheet = workbook.createSheet(exporter.getSheetName());

            exporter.exportSingle(id, sheet, styles);

            String fileName = exporter.getFileName(id);
            byte[] bytes = toBytes(workbook);

            log.info("[Export] Single {} id={} → {} bytes", type, id, bytes.length);
            return new ExportResult(fileName, bytes);
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Export failed: " + e.getMessage());
        }
    }

    /**
     * Export list of entities to Excel bytes.
     */
    public ExportResult exportList(ExportEntityType type) {
        EntityExporter exporter = getExporter(type);

        try (Workbook workbook = new XSSFWorkbook()) {
            ExcelStyleHelper styles = new ExcelStyleHelper(workbook);
            Sheet sheet = workbook.createSheet(exporter.getSheetName());

            exporter.exportList(sheet, styles);

            String fileName = exporter.getFileName(null);
            byte[] bytes = toBytes(workbook);

            log.info("[Export] List {} → {} bytes", type, bytes.length);
            return new ExportResult(fileName, bytes);
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Export failed: " + e.getMessage());
        }
    }

    private EntityExporter getExporter(ExportEntityType type) {
        EntityExporter exporter = exporterMap.get(type);
        if (exporter == null) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "No exporter registered for type: " + type);
        }
        return exporter;
    }

    private byte[] toBytes(Workbook workbook) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        return bos.toByteArray();
    }

    /**
     * Result wrapper for export operations.
     */
    public record ExportResult(String fileName, byte[] data) {}
}
