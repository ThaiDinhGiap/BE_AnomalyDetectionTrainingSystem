package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.DefectImportDto;
import com.sep490.anomaly_training_backend.dto.response.DefectResponse;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import com.sep490.anomaly_training_backend.enums.DefectType;
import com.sep490.anomaly_training_backend.enums.ImportStatus;
import com.sep490.anomaly_training_backend.enums.ImportType;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.DefectMapper;
import com.sep490.anomaly_training_backend.model.Defect;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.DefectRepository;
import com.sep490.anomaly_training_backend.repository.ProcessRepository;
import com.sep490.anomaly_training_backend.service.DefectService;
import com.sep490.anomaly_training_backend.service.ImportHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Defect defect = defectRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.DEFECT_NOT_FOUND));
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
                throw new AppException(ErrorCode.IMPORT_FAILED);
            }

            List<DefectResponse> responses = saveAllRows(sheet);

            saveImportPassHistory(currentUser, file);
            return responses;

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Import defect failed", e);
            List<ImportErrorItem> systemErrors = List.of(buildSystemError(e.getMessage()));
            saveImportFailHistory(currentUser, file, systemErrors);
            throw new AppException(ErrorCode.CANNOT_READ_EXCEL_FILE);
        }
    }

    private void validateImportFile(MultipartFile file) throws BadRequestException {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_IS_EMPTY);
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls"))) {
            throw new AppException(ErrorCode.INVALID_FILE_FORMAT);
        }
    }

    private Sheet getFirstSheet(Workbook workbook) throws BadRequestException {
        if (workbook.getNumberOfSheets() == 0) {
            throw new AppException(ErrorCode.EXCEL_SHEET_NOT_FOUND);
        }

        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            throw new AppException(ErrorCode.EXCEL_SHEET_NOT_FOUND);
        }

        return sheet;
    }

    private void validateAllRows(Sheet sheet, List<ImportErrorItem> errors) {
        List<DefectImportDto> parsedDtos = new ArrayList<>();
        for (int i = 2; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) continue;
            try {
                DefectImportDto dto = parseRowToImportDto(row, i + 1);
                parsedDtos.add(dto);
            } catch (AppException e) {
                errors.add(buildRowError(i + 1, "ROW", null, e.getMessage()));
            } catch (Exception e) {
                errors.add(buildRowError(i + 1, "ROW", null, "Unexpected error: " + e.getMessage()));
            }
        }

        // Validate no duplicate defectCode and defectDescription
        validateNoDuplicateDefectCode(parsedDtos, errors);
        validateNoDuplicateDefectDescription(parsedDtos, errors);
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
                .findByDefectCode(dto.getDefectCode())
                .orElseGet(Defect::new);

        applyImportDtoToDefect(dto, defect);

        return defectRepository.save(defect);
    }

    private void applyImportDtoToDefect(DefectImportDto dto, Defect defect) {
        defect.setDefectDescription(dto.getDefectDescription());
        defect.setDefectCode(dto.getDefectCode());
        defect.setDetectedDate(dto.getDetectedDate());
        defect.setNote(dto.getNote());
        defect.setOriginCause(dto.getOriginCause());
        defect.setOutflowCause(dto.getOutflowCause());
        defect.setCausePoint(dto.getCausePoint());
        defect.setOriginMeasures(dto.getOriginMeasures());
        defect.setOutflowMeasures(dto.getOutflowMeasures());
        defect.setDefectType(DefectType.valueOf(dto.getDefectType()));

        if (dto.getProcessCode() != null) {
            Process process = processRepository.findByCode(dto.getProcessCode())
                    .orElseThrow(() -> new AppException(ErrorCode.PROCESS_NOT_FOUND, "Process not found with code: " + dto.getProcessCode()));
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
            throw new AppException(ErrorCode.INVALID_CELL_VALUE, "Row " + excelRowNumber + ": " + fieldName + " must not be blank");
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
            throw new AppException(ErrorCode.INVALID_CELL_VALUE, "Row " + excelRowNumber + ": " + fieldName + " has invalid format");
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_CELL_VALUE, "Row " + excelRowNumber + ": " + fieldName + " has invalid value: " + getCellDisplayValue(cell));
        }
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
                .field("SYSTEM")
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
        String defectCode = getRequiredStringCellValue(
                row.getCell(1), "defectDescription", excelRowNumber
        );
        String defectDescription = getRequiredStringCellValue(
                row.getCell(2), "defectCode", excelRowNumber
        );

        String processCode = getRequiredStringCellValue(
                row.getCell(3), "processCode", excelRowNumber
        );

        LocalDate detectedDate = getLocalDateCellValue(
                row.getCell(4), "detectedDate", excelRowNumber
        );

        Boolean isEscaped = getEscapedCellValue(
                row.getCell(5), "isEscaped", excelRowNumber
        );

        String outflowCause = getOptionalStringCellValue(row.getCell(6));
        String originCause = getOptionalStringCellValue(row.getCell(7));
        String causePoint = getOptionalStringCellValue(row.getCell(8));
        String note = getOptionalStringCellValue(row.getCell(9));
        String originMeasures = getOptionalStringCellValue(row.getCell(10));
        String outflowMeasures = getOptionalStringCellValue(row.getCell(11));
        String defectType = getRequiredStringCellValue(
                row.getCell(12), "defectType", excelRowNumber
        );

//        Process process = processRepository.findByCode(processCode.trim()).orElseThrow(() -> new BadRequestException("Row " + excelRowNumber + ": Process not found: " + processCode));

        Process process = processRepository.findByCode(processCode.trim())
                .orElseThrow(() -> new AppException(ErrorCode.PROCESS_NOT_FOUND, "Row " + excelRowNumber + ": Process not found: " + processCode));
        return DefectImportDto.builder()
                .defectCode(defectCode)
                .defectDescription(defectDescription)
                .detectedDate(detectedDate)
                .note(note)
                .originCause(originCause)
                .outflowCause(outflowCause)
                .causePoint(causePoint)
                .originMeasures(originMeasures)
                .outflowMeasures(outflowMeasures)
                .defectType(defectType)
                .processCode(process.getCode())
                .excelRowNumber(excelRowNumber)
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

    /**
     * Validate that defectCode is unique across all rows in import
     * Rule: defectCode không được lặp lại trong file import
     */
    private void validateNoDuplicateDefectCode(List<DefectImportDto> parsedDtos, List<ImportErrorItem> errors) {
        Map<String, Integer> defectCodeCount = new HashMap<>();
        Map<String, Integer> firstOccurrenceRow = new HashMap<>();

        for (DefectImportDto dto : parsedDtos) {
            String code = dto.getDefectCode();
            defectCodeCount.put(code, defectCodeCount.getOrDefault(code, 0) + 1);

            if (!firstOccurrenceRow.containsKey(code)) {
                firstOccurrenceRow.put(code, dto.getExcelRowNumber() != null ? dto.getExcelRowNumber() : 0);
            }
        }

        // Find duplicates
        for (Map.Entry<String, Integer> entry : defectCodeCount.entrySet()) {
            if (entry.getValue() > 1) {
                // Find all rows with this duplicate code
                for (DefectImportDto dto : parsedDtos) {
                    if (dto.getDefectCode().equals(entry.getKey())) {
                        errors.add(buildRowError(
                                dto.getExcelRowNumber(),
                                "defectCode",
                                entry.getKey(),
                                "defectCode '" + entry.getKey() + "' is duplicated in file import (" + entry.getValue() + " occurrences)"
                        ));
                    }
                }
            }
        }
    }

    /**
     * Validate that defectDescription is unique across all rows in import
     * Rule: defectDescription không được lặp lại, ngay cả khi defectCode khác nhau
     */
    private void validateNoDuplicateDefectDescription(List<DefectImportDto> parsedDtos, List<ImportErrorItem> errors) {
        Map<String, Integer> descriptionCount = new HashMap<>();

        for (DefectImportDto dto : parsedDtos) {
            String description = dto.getDefectDescription();
            descriptionCount.put(description, descriptionCount.getOrDefault(description, 0) + 1);
        }

        // Find duplicates
        for (Map.Entry<String, Integer> entry : descriptionCount.entrySet()) {
            if (entry.getValue() > 1) {
                // Find all rows with this duplicate description
                for (DefectImportDto dto : parsedDtos) {
                    if (dto.getDefectDescription().equals(entry.getKey())) {
                        errors.add(buildRowError(
                                dto.getExcelRowNumber(),
                                "defectDescription",
                                entry.getKey(),
                                "defectDescription '" + entry.getKey() + "' is duplicated in file import (" + entry.getValue() + " occurrences)"
                        ));
                    }
                }
            }
        }
    }
}
