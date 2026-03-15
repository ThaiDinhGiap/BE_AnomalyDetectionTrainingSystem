package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.DefectImportDto;
import com.sep490.anomaly_training_backend.dto.request.ImageData;
import com.sep490.anomaly_training_backend.dto.response.DefectResponse;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import com.sep490.anomaly_training_backend.enums.DefectType;
import com.sep490.anomaly_training_backend.enums.ImportStatus;
import com.sep490.anomaly_training_backend.enums.ImportType;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.DefectMapper;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.DefectService;
import com.sep490.anomaly_training_backend.service.ImportHistoryService;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import com.sep490.anomaly_training_backend.service.minio.ImportImageHandlerService;
import com.sep490.anomaly_training_backend.util.DefectCodeGenerator;
import com.sep490.anomaly_training_backend.util.helper.DefectImportHelper;
import com.sep490.anomaly_training_backend.util.validator.DefectImportValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefectServiceImpl implements DefectService {

    private final DefectRepository defectRepository;
    private final DefectMapper defectMapper;
    private final ProcessRepository processRepository;
    private final ProductRepository productRepository;
    private final ImportHistoryService importHistoryService;
    private final DefectImportValidator importValidator;
    private final DefectImportHelper importHelper;
    private final ImportImageHandlerService importImageHandlerService;
    private final AttachmentService attachmentService;
    private final DefectCodeGenerator defectCodeGenerator;

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
    public List<DefectResponse> importDefect(User currentUser, MultipartFile file) {
        validateImportFile(file);

        List<ImportErrorItem> errors = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = getFirstSheet(workbook);
            List<DefectImportDto> parsedRows = importHelper.parseExcelRows(sheet, errors);

            if (!errors.isEmpty()) {
                saveImportFailHistory(currentUser, file, errors);
                throw new AppException(ErrorCode.IMPORT_PARSE_ERROR);
            }

            // Step 4: Validate only file data (NO DB validation)
            importValidator.validateFileData(parsedRows, errors);

            if (!errors.isEmpty()) {
                saveImportFailHistory(currentUser, file, errors);
                throw new AppException(ErrorCode.IMPORT_VALIDATION_ERROR);
            }

            // Step 5: Upsert all rows (changed from insert to upsert)
            List<DefectResponse> responses = upsertAllRows(parsedRows, currentUser, errors);

            // Step 6: Save success history
            saveImportPassHistory(currentUser, file);

            return responses;

        } catch (AppException e) {
            saveImportFailHistory(currentUser, file, errors);
            throw e;
        } catch (Exception e) {
            log.error("Import defect failed", e);
            if (errors.isEmpty()) {
                errors.add(buildSystemError("System error: " + e.getMessage()));
                saveImportFailHistory(currentUser, file, errors);
            }
            throw new AppException(ErrorCode.CANNOT_READ_EXCEL_FILE);
        }
    }

    private void validateImportFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_IS_EMPTY);
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls"))) {
            throw new AppException(ErrorCode.INVALID_FILE_FORMAT);
        }
    }

    private Sheet getFirstSheet(Workbook workbook)  {
        if (workbook.getNumberOfSheets() == 0) {
            throw new AppException(ErrorCode.EXCEL_SHEET_NOT_FOUND);
        }

        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            throw new AppException(ErrorCode.EXCEL_SHEET_NOT_FOUND);
        }

        return sheet;
    }


    /**
     * Upsert all parsed rows into database
     * - If defectCode is null: always INSERT new
     * - If defectCode is not null: find existing, UPDATE if found OR CREATE if new
     * - Handle images from Excel rows
     * - Soft-delete old attachments when updating
     */
    private List<DefectResponse> upsertAllRows(
            List<DefectImportDto> parsedRows,
            User user, List<ImportErrorItem> errors) {

        List<DefectResponse> responses = new ArrayList<>();

        for (DefectImportDto dto : parsedRows) {
            // Check if this is an update (defectCode is not null AND record exists)
            boolean isUpdate = dto.getDefectCode() != null
                    && defectRepository.findByDefectCode(dto.getDefectCode()).isPresent();

            Defect defect = upsertDefect(dto, errors);

            // If updating, soft-delete old attachments before handling new ones
            if (isUpdate && defect.getId() != null) {
                attachmentService.deleteAttachments("DEFECT", defect.getId());
            }
            handleDefectImages(dto.getImageData(), defect, user);
            responses.add(defectMapper.toDto(defect));
        }

        return responses;
    }

    private Defect upsertDefect(DefectImportDto dto, List<ImportErrorItem> errors) {
        // Find existing by defectCode only if defectCode is not null
        Defect defect;
        if (dto.getDefectCode() != null && !dto.getDefectCode().trim().isEmpty()) {
            defect = defectRepository
                    .findByDefectCode(dto.getDefectCode())
                    .orElseThrow(() -> addErrorAndReturn(
                            errors,
                            dto.getExcelRowNumber(),
                            "defectCode",
                            dto.getProductCode(),
                            "Defect not found with code: " + dto.getProductCode(),
                            ErrorCode.DEFECT_NOT_FOUND
                    ));
        } else {
            // If defectCode is null, always create new
            defect = new Defect();
        }

        // If updating, soft-delete old attachments before handling new ones
        if (defect.getId() != null) {
            attachmentService.deleteAttachments("DEFECT", defect.getId());
        }

        // Apply all fields
        applyImportDtoToDefect(dto, defect, errors);
        Defect saved = defectRepository.save(defect);

        boolean isNew = defect.getId() == null;
        log.info("Upserted Defect: code={}, id={}, isNew={}",
                dto.getDefectCode(), saved.getId(), isNew);
        return saved;
    }

    private void applyImportDtoToDefect(DefectImportDto dto, Defect defect, List<ImportErrorItem> errors) {
        // Resolve and validate Process
        Process process = null;
        if (dto.getProcessCode() != null && !dto.getProcessCode().trim().isEmpty()) {
            process = processRepository.findByCode(dto.getProcessCode())
                    .orElseThrow(() -> addErrorAndReturn(
                            errors,
                            dto.getExcelRowNumber(),
                            "processCode",
                            dto.getProcessCode(),
                            "Process not found with code: " + dto.getProcessCode(),
                            ErrorCode.PROCESS_NOT_FOUND
                            ));
        }

        // Resolve and validate Product
        Product product = null;
        if (dto.getProductCode() != null && !dto.getProductCode().trim().isEmpty()) {
            product = productRepository.findByCode(dto.getProductCode())
                    .orElseThrow(() -> addErrorAndReturn(
                            errors,
                            dto.getExcelRowNumber(),
                            "productCode",
                            dto.getProductCode(),
                            "Product not found with code: " + dto.getProductCode(),
                            ErrorCode.PRODUCT_NOT_FOUND
                    ));
        }
        if (dto.getDefectCode() != null) {
            defect.setDefectCode(dto.getDefectCode());
        }
        else {
            defect.setDefectCode(defectCodeGenerator.generateDefectCode());
        }

        defect.setDefectDescription(dto.getDefectDescription());
        defect.setDetectedDate(dto.getDetectedDate());
        defect.setNote(dto.getNote());
        defect.setOriginCause(dto.getOriginCause());
        defect.setOutflowCause(dto.getOutflowCause());
        defect.setCausePoint(dto.getCausePoint());
        defect.setOriginMeasures(dto.getOriginMeasures());
        defect.setOutflowMeasures(dto.getOutflowMeasures());
        if (dto.getIsEscape()) {
            defect.setDefectType(DefectType.DEFECTIVE_GOODS);
        }
        else if (dto.getStartledClaim()) {
            defect.setDefectType(DefectType.STARTLED_CLAIM);
        }
        else {
            defect.setDefectType(DefectType.CLAIM);
        }
        // Set 4 new fields from import
        defect.setCustomer(dto.getCustomer());
        defect.setQuantity(dto.getQuantity());
        defect.setConclusion(dto.getConclusion());
        
        // Set references
        defect.setProcess(process);
        defect.setProduct(product);
    }

    /**
     * Handle images for Defect from Excel row
     * - Extract images từ row
     * - Upload new images via ImportImageHandlerService
     */
    private void handleDefectImages(ImageData imageData, Defect defect, User user) {
        try {
            if (defect == null || defect.getId() == null) {
                log.debug("Defect has no ID, skipping image handling");
                return;
            }

            log.info("Handling images for Defect id={}", defect.getId());

            // Note: Image handling service is injected and will be called here
            // Currently images are not extracted from Excel in this import
            // Future: If Excel format includes images, call importImageHandlerService here
            importImageHandlerService.handleRowImages(imageData, "DEFECT", defect.getId(), user.getUsername());

        } catch (Exception e) {
            if (defect != null && defect.getId() != null) {
                log.error("Error handling images for Defect id={}: {}", defect.getId(), e.getMessage());
            } else {
                log.error("Error handling images for Defect: {}", e.getMessage());
            }
            // Don't throw - ảnh handling fail không nên block main import
        }
    }

    // ...existing code...

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
    private AppException addErrorAndReturn(
            List<ImportErrorItem> errors,
            Integer rowNumber,
            String field,
            String value,
            String message,
            ErrorCode errorCode) {

        errors.add(ImportErrorItem.builder()
                .rowNumber(rowNumber)
                .field(field)
                .value(value)
                .message(message)
                .build());

        return new AppException(errorCode);
    }
}
