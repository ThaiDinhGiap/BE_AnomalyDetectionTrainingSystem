package com.sep490.anomaly_training_backend.service.export;

import com.sep490.anomaly_training_backend.enums.ExportEntityType;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * Strategy interface for entity-specific Excel export logic.
 * Each entity type implements this to define how its data is written to a sheet.
 */
public interface EntityExporter {

    /** Which entity type this exporter handles */
    ExportEntityType getType();

    /** Export a single entity by ID into the given sheet */
    void exportSingle(Long id, Sheet sheet, ExcelStyleHelper styles);

    /** Export all/list of entities into the given sheet */
    void exportList(Sheet sheet, ExcelStyleHelper styles);

    /** Display name for the Excel sheet tab */
    String getSheetName();

    /** Generate file name. If id is null, it's a list export. */
    String getFileName(Long id);
}
