package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.DefectImportDto;
import com.sep490.anomaly_training_backend.dto.response.DefectResponse;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import com.sep490.anomaly_training_backend.enums.ImportStatus;
import com.sep490.anomaly_training_backend.enums.ImportType;
import com.sep490.anomaly_training_backend.mapper.DefectMapper;
import com.sep490.anomaly_training_backend.model.Defect;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.DefectRepository;
import com.sep490.anomaly_training_backend.repository.ProcessRepository;
import com.sep490.anomaly_training_backend.service.DefectService;
import com.sep490.anomaly_training_backend.service.ImportHistoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefectServiceImpl implements DefectService {

    private final DefectRepository defectRepository;
    private final DefectMapper defectMapper;
    private final ProcessRepository processRepository;
    private final ImportHistoryService importHistoryService;

    @Override
    public List<DefectResponse> getDefectBySupervisor(Long supervisorId) {
        return defectRepository.findAllBySupervisorAndDeleteFlagFalse(supervisorId)
                               .stream()
                               .map(defectMapper::toDto).toList();
    }

    @Override
    public List<DefectResponse> getDefectByProductLine(Long productLineId) {
        return defectRepository.findAllByProductLineAndDeleteFlagFalse(productLineId)
                .stream()
                .map(defectMapper::toDto).toList();
    }

    @Override
    public List<DefectResponse> getDefectByProcess(Long processId) {
        return defectRepository.findByProcessIdAndDeleteFlagFalse(processId)
                .stream()
                .map(defectMapper::toDto).toList();
    }

    @Override
    public DefectResponse getDefectById(Long id) {
        Defect defect = defectRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Defect not found"));
        return defectMapper.toDto(defect);
    }

    @Override
    public Boolean checkExistDefectDescription(String defectDescription) {
         return defectRepository.existsActiveByDefectDescriptionIgnoreCase(defectDescription);
    }

    @Override
    public List<DefectResponse> importDefect(User currentUser, MultipartFile file) throws BadRequestException {
        validateImportFile(file);

        List<ImportErrorItem> errors = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = getFirstSheet(workbook);

            validateAllRows(sheet, errors);

            if (!errors.isEmpty()) {
                saveImportFailHistory(currentUser, file, errors);
                throw new BadRequestException("Import failed. Please check import history.");
            }

            List<DefectResponse> responses = saveAllRows(sheet);

            saveImportPassHistory(currentUser, file);
            return responses;

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Import defect failed", e);

            List<ImportErrorItem> systemErrors = List.of(
                    buildSystemError(e.getMessage())
            );

            saveImportFailHistory(currentUser, file, systemErrors);
            throw new BadRequestException("Cannot read excel file: " + e.getMessage());
        }
    }
    private void validateImportFile(MultipartFile file) throws BadRequestException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null ||
                (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls"))) {
            throw new BadRequestException("Only .xls or .xlsx files are supported");
        }
    }
    private Sheet getFirstSheet(Workbook workbook) throws BadRequestException {
        if (workbook.getNumberOfSheets() == 0) {
            throw new BadRequestException("Excel file does not contain any sheet");
        }

        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            throw new BadRequestException("Cannot read first sheet");
        }

        return sheet;
    }
    private void validateAllRows(Sheet sheet, List<ImportErrorItem> errors) {
        for (int i = 2; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            if (isRowEmpty(row)) {
                continue;
            }

            try {
                parseRowToImportDto(row, i + 1);
            } catch (BadRequestException e) {
                errors.add(buildRowError(i + 1, "ROW", null, e.getMessage()));
            } catch (Exception e) {
                errors.add(buildRowError(i + 1, "ROW", null, "Unexpected error: " + e.getMessage()));
            }
        }
    }
    private List<DefectResponse> saveAllRows(Sheet sheet) throws BadRequestException {
        List<DefectResponse> responses = new ArrayList<>();

        for (int i = 2; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            if (isRowEmpty(row)) {
                continue;
            }

            DefectImportDto dto = parseRowToImportDto(row, i + 1);
            Defect savedDefect = upsertDefect(dto);
            responses.add(defectMapper.toDto(savedDefect));
        }

        return responses;
    }

    private Defect upsertDefect(DefectImportDto dto) {
        Defect defect = defectRepository
                .findByDefectDescriptionIgnoreCase(dto.getDefectDescription().trim())
                .orElseGet(Defect::new);

        applyImportDtoToDefect(dto, defect);

        return defectRepository.save(defect);
    }
    private void applyImportDtoToDefect(DefectImportDto dto, Defect defect) {
        defect.setDefectDescription(dto.getDefectDescription());
        defect.setDetectedDate(dto.getDetectedDate());
        defect.setIsEscaped(dto.getIsEscaped());
        defect.setNote(dto.getNote());
        defect.setOriginCause(dto.getOriginCause());
        defect.setOutflowCause(dto.getOutflowCause());
        defect.setCausePoint(dto.getCausePoint());

        if (dto.getProcessCode() != null) {
            Process process = processRepository.findByCode(dto.getProcessCode())
                    .orElseThrow(() -> new IllegalArgumentException("Process not found with code: " + dto.getProcessCode()));
            defect.setProcess(process);
        } else {
            defect.setProcess(null);
        }
    }
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int c = 0; c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellDisplayValue(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }
    private String getRequiredStringCellValue(Cell cell, String fieldName, int excelRowNumber) throws BadRequestException {
        String value = getOptionalStringCellValue(cell);

        if (value == null || value.isBlank()) {
            throw new BadRequestException("Row " + excelRowNumber + ": " + fieldName + " must not be blank");
        }

        return value.trim();
    }
    private String getOptionalStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case STRING -> normalize(cell.getStringCellValue());
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }

                double value = cell.getNumericCellValue();
                if (value == (long) value) {
                    yield String.valueOf((long) value);
                }
                yield String.valueOf(value);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> normalize(cell.toString());
            case BLANK -> null;
            default -> normalize(cell.toString());
        };
    }
    private LocalDate getLocalDateCellValue(Cell cell, String fieldName, int excelRowNumber) throws BadRequestException {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }

            if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                if (value.isBlank()) {
                    return null;
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return LocalDate.parse(value, formatter);
            }

            throw new BadRequestException("Row " + excelRowNumber + ": " + fieldName + " has invalid format");
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException(
                    "Row " + excelRowNumber + ": " + fieldName + " has invalid value: " + getCellDisplayValue(cell)
            );
        }
    }

    private Boolean parseBooleanText(String value, String fieldName, int excelRowNumber) throws BadRequestException {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toLowerCase();

        return switch (normalized) {
            case "true", "1", "yes", "y" -> true;
            case "false", "0", "no", "n" -> false;
            default -> throw new BadRequestException(
                    "Row " + excelRowNumber + ": " + fieldName + " must be true/false, yes/no, or 1/0"
            );
        };
    }
    private String getCellDisplayValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }

                double value = cell.getNumericCellValue();
                if (value == (long) value) {
                    yield String.valueOf((long) value);
                }
                yield String.valueOf(value);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.toString();
            case BLANK -> "";
            default -> cell.toString();
        };
    }
    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
    private void saveImportFailHistory(User currentUser, MultipartFile file, List<ImportErrorItem> errors) {
        importHistoryService.saveHistory(
                currentUser,
                file.getOriginalFilename(),
                ImportType.DEFECT_IMPORT,
                ImportStatus.FAIL,
                errors
        );
    }
    private ImportErrorItem buildSystemError(String message) {
        return ImportErrorItem.builder()
                .rowNumber(null)
                .field("SYSTEM")
                .value(null)
                .message(message)
                .build();
    }
    private void saveImportPassHistory(User currentUser, MultipartFile file) {
        importHistoryService.saveHistory(
                currentUser,
                file.getOriginalFilename(),
                ImportType.DEFECT_IMPORT,
                ImportStatus.PASS,
                List.of()
        );
    }
    private ImportErrorItem buildRowError(Integer rowNumber, String field, String value, String message) {
        return ImportErrorItem.builder()
                .rowNumber(rowNumber)
                .field(field)
                .value(value)
                .message(message)
                .build();
    }
    private DefectImportDto parseRowToImportDto(Row row, int excelRowNumber) throws BadRequestException {
        String defectDescription = getRequiredStringCellValue(
                row.getCell(1), "defectDescription", excelRowNumber
        );

        String processCode = getRequiredStringCellValue(
                row.getCell(2), "processCode", excelRowNumber
        );

        LocalDate detectedDate = getLocalDateCellValue(
                row.getCell(3), "detectedDate", excelRowNumber
        );

        Boolean isEscaped = getEscapedCellValue(
                row.getCell(4), "isEscaped", excelRowNumber
        );

        String outflowCause = getOptionalStringCellValue(row.getCell(5));
        String originCause = getOptionalStringCellValue(row.getCell(6));
        String causePoint = getOptionalStringCellValue(row.getCell(7));
        String note = getOptionalStringCellValue(row.getCell(8));

        Process process = processRepository.findByCode(processCode.trim()).orElseThrow(() -> new BadRequestException("Row " + excelRowNumber + ": Process not found: " + processCode));

        return DefectImportDto.builder()
                .defectDescription(defectDescription)
                .detectedDate(detectedDate)
                .isEscaped(isEscaped)
                .note(note)
                .originCause(originCause)
                .outflowCause(outflowCause)
                .causePoint(causePoint)
                .processCode(process.getCode())
                .build();
    }
    private Boolean getEscapedCellValue(Cell cell, String fieldName, int excelRowNumber) throws BadRequestException {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        String value = getCellDisplayValue(cell);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String normalized = value.trim().toLowerCase();

        return switch (normalized) {
            case "có", "co" -> true;
            case "không", "khong" -> false;
            default -> throw new BadRequestException(
                    "Row " + excelRowNumber + ": " + fieldName + " must be 'Có' or 'Không'"
            );
        };
    }

}
