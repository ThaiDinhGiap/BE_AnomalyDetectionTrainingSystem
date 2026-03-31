package com.sep490.anomaly_training_backend.service.defect.impl;

import com.sep490.anomaly_training_backend.dto.request.DefectImportDto;
import com.sep490.anomaly_training_backend.dto.request.ImageData;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import com.sep490.anomaly_training_backend.dto.response.ProductResponse;
import com.sep490.anomaly_training_backend.dto.response.defect.DefectCoverageResponse;
import com.sep490.anomaly_training_backend.dto.response.defect.DefectInProcess;
import com.sep490.anomaly_training_backend.dto.response.defect.DefectResponse;
import com.sep490.anomaly_training_backend.enums.DefectType;
import com.sep490.anomaly_training_backend.enums.ImportStatus;
import com.sep490.anomaly_training_backend.enums.ImportType;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.DefectMapper;
import com.sep490.anomaly_training_backend.model.Attachment;
import com.sep490.anomaly_training_backend.model.Defect;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.model.Product;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.DefectRepository;
import com.sep490.anomaly_training_backend.repository.ProcessRepository;
import com.sep490.anomaly_training_backend.repository.ProductLineRepository;
import com.sep490.anomaly_training_backend.repository.ProductRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleRepository;
import com.sep490.anomaly_training_backend.service.ImportHistoryService;
import com.sep490.anomaly_training_backend.service.ProductService;
import com.sep490.anomaly_training_backend.service.defect.DefectService;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import com.sep490.anomaly_training_backend.service.minio.ImportImageHandlerService;
import com.sep490.anomaly_training_backend.util.DefectCodeGenerator;
import com.sep490.anomaly_training_backend.util.helper.DefectImportHelper;
import com.sep490.anomaly_training_backend.util.validator.DefectImportValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefectServiceImpl implements DefectService {

    private final DefectRepository defectRepository;
    private final DefectMapper defectMapper;
    private final ProcessRepository processRepository;
    private final ProductRepository productRepository;
    private final ProductLineRepository productLineRepository;
    private final ImportHistoryService importHistoryService;
    private final DefectImportValidator importValidator;
    private final DefectImportHelper importHelper;
    private final ImportImageHandlerService importImageHandlerService;
    private final AttachmentService attachmentService;
    private final DefectCodeGenerator defectCodeGenerator;
    private final ProductService productService;
    private final TrainingSampleRepository trainingSampleRepository;

    @Override
    public List<DefectResponse> getDefectBySupervisor(Long supervisorId) {
        return defectRepository.findAllBySupervisorAndDeleteFlagFalseOrderByCreatedAtDesc(supervisorId)
                .stream()
                .map(defectMapper::toDto).toList();
    }

    @Override
    public List<DefectResponse> getDefectByProductLine(Long productLineId) {
        List<Defect> defects = defectRepository.findAllByProductLineAndDeleteFlagFalseOrderByCreatedAtDesc(productLineId);
        return defects.stream().map(defect -> {
            DefectResponse response = defectMapper.toDto(defect);
            if (defect.getProduct() != null) {
                ProductResponse productResponse = productService.getProductById(defect.getProduct().getId());
                response.setProduct(productResponse);
            }
            addAttachment(response);
            return response;
        }).toList();
    }

    @Override
    public List<DefectResponse> getDefectByProcess(Long processId) {
        return defectRepository.findByProcessIdAndDeleteFlagFalse(processId)
                .stream()
                .map(defect -> {
                    DefectResponse response = defectMapper.toDto(defect);
                    if (defect.getProduct() != null) {
                        ProductResponse productResponse = productService.getProductById(defect.getProduct().getId());
                        response.setProduct(productResponse);
                    }
                    addAttachment(response);
                    return response;
                }).toList();
    }

    @Override
    public DefectResponse getDefectById(Long id) {
        Defect defect = defectRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.DEFECT_NOT_FOUND));
        DefectResponse response = defectMapper.toDto(defect);
        if (defect.getProduct() != null) {
            ProductResponse productResponse = productService.getProductById(defect.getProduct().getId());
            response.setProduct(productResponse);
        }
        return addAttachment(defectMapper.toDto(defect));
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
            ProductLine productLine = getProductLineFromHeader(sheet);
            if (productLine == null) {
                errors.add(buildSystemError("ProductLine not found in file header (Row 1)"));
                throw new AppException(ErrorCode.PRODUCT_LINE_NOT_IN_HEADER);
            }
            // Step 4: Validate only file data (NO DB validation)
            importValidator.validateFileData(parsedRows, errors);

            if (!errors.isEmpty()) {
                throw new AppException(ErrorCode.IMPORT_VALIDATION_ERROR);
            }

            // Step 5: Upsert all rows (changed from insert to upsert)
            List<DefectResponse> responses = upsertAllRows(parsedRows, productLine, currentUser, errors);

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

    @Override
    public DefectCoverageResponse getCoverageInProductLine(Long productLineId) {
        List<Defect> defects = defectRepository.findDefectsWithoutTrainingSampleOrderByCreatedAtDesc(productLineId);
        int totalDefect = defectRepository.findAllByProductLineAndDeleteFlagFalseOrderByCreatedAtDesc(productLineId).size();
        int totalTrainingSample = trainingSampleRepository.findByProductLineIdAndDeleteFlagFalseOrderByCreatedAtDesc(productLineId).size();
        List<Defect> totalDefectInProductLIne = defectRepository.findAllByProductLineAndDeleteFlagFalseOrderByCreatedAtDesc(productLineId);
        Double coverage = totalDefectInProductLIne.isEmpty() ? 0.0 : (double) (totalDefectInProductLIne.size() - defects.size()) / totalDefectInProductLIne.size() * 100;
        return DefectCoverageResponse.builder()
                .defects(defects.stream().map(defectMapper::toDto).toList())
                .coverageRate(coverage)
                .totalDefect((long) totalDefect)
                .totalTrainingSample((long) totalTrainingSample)
                .build();
    }

    @Override
    public List<DefectInProcess> countDefectInProcess(Long productLineId) {
        List<Process> processes = processRepository.findByProductLineIdAndDeleteFlagFalse(productLineId);
        List<DefectInProcess> defectsInProcess = new ArrayList<>();
        for (Process process : processes) {
            List<Defect> defects = defectRepository.findByProcessIdAndDeleteFlagFalse(process.getId());
            long totalDefect = defects.size();
            long totalDefectiveGood = 0;
            long totalClaim = 0;
            long totalStartedClaim = 0;
            for (Defect defect : defects) {
                if (defect.getDefectType().equals(DefectType.DEFECTIVE_GOODS)) {
                    totalDefectiveGood++;
                } else if (defect.getDefectType().equals(DefectType.STARTLED_CLAIM)) {
                    totalStartedClaim++;
                } else {
                    totalClaim++;
                }
            }
            DefectInProcess defectInProcess = DefectInProcess.builder()
                    .processId(process.getId())
                    .processCode(process.getCode())
                    .processName(process.getName())
                    .totalDefects(totalDefect)
                    .totalDefectiveGood(totalDefectiveGood)
                    .totalClaim(totalClaim)
                    .totalStartledClaim(totalStartedClaim)
                    .build();
            defectsInProcess.add(defectInProcess);
        }
        return defectsInProcess;
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

    private Sheet getFirstSheet(Workbook workbook) {
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
     * Get ProductLine from Excel header row (Row 1)
     */
    private ProductLine getProductLineFromHeader(Sheet sheet) {
        try {
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return null;
            }

            Cell cell = headerRow.getCell(1);
            if (cell == null) {
                return null;
            }

            String productLineCode = getCellStringValue(cell);
            if (productLineCode == null || productLineCode.trim().isEmpty()) {
                return null;
            }

            return productLineRepository.findByCode(productLineCode.trim()).orElse(null);
        } catch (Exception e) {
            log.error("Error reading ProductLine from header", e);
            return null;
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                double value = cell.getNumericCellValue();
                if (value == (long) value) {
                    return String.valueOf((long) value);
                }
                return String.valueOf(value);
            default:
                return cell.toString();
        }
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
            ProductLine productLine,
            User user, List<ImportErrorItem> errors) {

        List<DefectResponse> responses = new ArrayList<>();

        for (DefectImportDto dto : parsedRows) {
            // Check if this is an update (defectCode is not null AND record exists)
            boolean isUpdate = dto.getDefectCode() != null
                    && defectRepository.findByDefectCode(dto.getDefectCode()).isPresent();

            Defect defect = upsertDefect(dto, productLine, errors);

            // If updating, soft-delete old attachments before handling new ones
            if (isUpdate && defect.getId() != null) {
                attachmentService.deleteAttachments("DEFECT", defect.getId());
            }
            handleDefectImages(dto.getImageData(), defect, user);
            responses.add(defectMapper.toDto(defect));
        }

        return responses;
    }

    private Defect upsertDefect(DefectImportDto dto, ProductLine productLine, List<ImportErrorItem> errors) {
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
        applyImportDtoToDefect(dto, productLine, defect, errors);
        Defect saved = defectRepository.save(defect);

        boolean isNew = defect.getId() == null;
        log.info("Upserted Defect: code={}, id={}, isNew={}",
                dto.getDefectCode(), saved.getId(), isNew);
        return saved;
    }

    private void applyImportDtoToDefect(DefectImportDto dto, ProductLine productLine, Defect defect, List<ImportErrorItem> errors) {
        // Resolve and validate Process
        Process process = null;
        if (dto.getProcessCode() != null && !dto.getProcessCode().trim().isEmpty()) {
            process = processRepository.findByProductLineCodeAndCode(productLine.getCode(), dto.getProcessCode())
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
        } else {
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
        } else if (dto.getStartledClaim()) {
            defect.setDefectType(DefectType.STARTLED_CLAIM);
        } else {
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

    private DefectResponse addAttachment(DefectResponse defectResponse) {
        if (Objects.isNull(defectResponse)) {
            return null;
        }
        List<Attachment> attachments = attachmentService.getAttachmentsByEntity("DEFECT", defectResponse.getDefectId());
        List<String> imageUrls = attachments.stream()
                .map(Attachment::getUrl)
                .toList();
        defectResponse.setAttachmentUrls(imageUrls);
        return defectResponse;
    }
}
