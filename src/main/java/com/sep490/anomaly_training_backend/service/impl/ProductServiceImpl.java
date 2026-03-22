package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.ImageData;
import com.sep490.anomaly_training_backend.dto.request.ProductImportDto;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.dto.response.ProductResponse;
import com.sep490.anomaly_training_backend.enums.ImportStatus;
import com.sep490.anomaly_training_backend.enums.ImportType;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.ProductMapper;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.ImportHistoryService;
import com.sep490.anomaly_training_backend.service.ProductService;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import com.sep490.anomaly_training_backend.service.minio.ImportImageHandlerService;
import com.sep490.anomaly_training_backend.util.helper.ProductImportHelper;
import com.sep490.anomaly_training_backend.util.validator.ProductImportValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductLineRepository productLineRepository;
    private final ProductMapper productMapper;
    private final ImportHistoryService importHistoryService;
    private final ProductImportHelper importHelper;
    private final ProductImportValidator importValidator;
    private final ImportImageHandlerService importImageHandlerService;
    private final AttachmentService attachmentService;
    private final ProductProcessRepository productProcessRepository;

    @Override
    public List<ProductResponse> importProduct(User user, Long productLineId, MultipartFile productFile) {
        List<ImportErrorItem> errors = new ArrayList<>();
        ProductLine productLine = productLineRepository.findById(productLineId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_LINE_NOT_FOUND));

        try (Workbook workbook = WorkbookFactory.create(productFile.getInputStream())) {
            validateImportFile(productFile);
            Sheet sheet = getFirstSheet(workbook);

            // Step 1: Parse rows with merge cell handling
            List<ProductImportDto> parsedRows = importHelper.parseExcelRows(sheet, errors);

            if (!errors.isEmpty()) {
                throw new AppException(ErrorCode.IMPORT_PARSE_ERROR);
            }

            // Step 2: Validate file data (NO DB check)
            importValidator.validateFileData(parsedRows, errors);

            if (!errors.isEmpty()) {
                throw new AppException(ErrorCode.IMPORT_VALIDATION_ERROR);
            }

            // Step 3: Process all rows with proper error handling
            List<ProductResponse> responses = processAllRows(parsedRows, productLine, user);

            // Step 4: If any errors occurred during processing, save them
            if (!errors.isEmpty()) {
                throw new AppException(ErrorCode.IMPORT_FAILED);
            }

            // Step 5: Save success history
            saveImportPassHistory(user, productFile);

            return responses;

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Import product failed", e);
            if (errors.isEmpty()) {
                errors.add(buildSystemError("System error: " + e.getMessage()));
                saveImportFailHistory(user, productFile, errors);
            }
            throw new AppException(ErrorCode.CANNOT_READ_EXCEL_FILE);
        }
    }

    @Override
    public List<ProductResponse> getProductsByProductLineId(Long productLineId) {
        ProductLine productLine = productLineRepository.findById(productLineId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_LINE_NOT_FOUND));
        return productRepository.findByProductLineIdAndDeleteFlagFalse(productLineId)
                                .stream()
                                .map(productMapper::toDto).toList();
    }

    /**
     * Process all rows with proper error handling
     * - Finds existing or creates new Product
     * - Records all errors in importFailHistory
     */
    private List<ProductResponse> processAllRows(
            List<ProductImportDto> parsedRows,
            ProductLine productLine,
            User user) {

        List<ProductResponse> responses = new ArrayList<>();

        for (ProductImportDto dto : parsedRows) {
            // Step 1: Find or create Product by productCode
            Product product = findOrCreateProduct(dto);
            // Step 2: Update Product fields
            updateProductFields(product, dto);
            //Step 3: Apply all processes to product
            List<Process> processes = productLine.getProcesses();
            for (Process process : processes) {
                ProductProcess productProcess = productProcessRepository.findByProductIdAndProcessId(product.getId(), process.getId())
                        .orElseGet(ProductProcess::new);
                productProcess.setProduct(product);
                productProcess.setProcess(process);
                productProcessRepository.save(productProcess);
            }
            // Step 4: Save to database
            Product saved = productRepository.save(product);
            handleProductImages(dto.getImageData(), saved, user);
            responses.add(productMapper.toDto(saved));
        }
        return responses;
    }

    /**
     * Find existing Product or create new one
     * - Lookup by productCode
     * - If found: return existing (will be updated)
     * - If not found: create new
     */
    private Product findOrCreateProduct(ProductImportDto dto) {
        Product product;

        // Lookup by productCode
        product = productRepository.findByCode(dto.getProductCode())
                .orElseGet(Product::new);

        return product;
    }

    /**
     * Update all Product fields from DTO
     */
    private void updateProductFields(Product product, ProductImportDto dto) {
        product.setCode(dto.getProductCode());
        product.setName(dto.getProductName());
        product.setDescription(dto.getDescription());
    }

    /**
     * Validate import file
     */
    private void validateImportFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_IS_EMPTY);
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls"))) {
            throw new AppException(ErrorCode.INVALID_FILE_FORMAT);
        }
    }

    /**
     * Get first sheet from workbook
     */
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
     * Save import fail history
     */
    private void saveImportFailHistory(User user, MultipartFile file, List<ImportErrorItem> errors) {
        try {
            importHistoryService.saveHistory(
                    user,
                    file.getOriginalFilename(),
                    ImportType.PRODUCT_IMPORT,
                    ImportStatus.FAIL,
                    errors
            );
        } catch (Exception e) {
            log.error("Error saving import fail history: {}", e.getMessage());
        }
    }

    /**
     * Save import pass history
     */
    private void saveImportPassHistory(User user, MultipartFile file) {
        try {
            importHistoryService.saveHistory(
                    user,
                    file.getOriginalFilename(),
                    ImportType.PRODUCT_IMPORT,
                    ImportStatus.PASS,
                    List.of()
            );
        } catch (Exception e) {
            log.error("Error saving import pass history: {}", e.getMessage());
        }
    }

    /**
     * Build system error item
     */
    private ImportErrorItem buildSystemError(String message) {
        return ImportErrorItem.builder()
                .field("SYSTEM")
                .message(message)
                .build();
    }

    private void handleProductImages(ImageData imageData, Product product, User user) {
        try {
            if (product == null || product.getId() == null) {
                log.debug("Defect has no ID, skipping image handling");
                return;
            }

            log.info("Handling images for Defect id={}", product.getId());

            importImageHandlerService.handleRowImages(imageData, "PRODUCT", product.getId(), user.getUsername());

        } catch (Exception e) {
            if (product != null && product.getId() != null) {
                log.error("Error handling images for Product id={}: {}", product.getId(), e.getMessage());
            } else {
                log.error("Error handling images for Product: {}", e.getMessage());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.info("Fetching product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.isDeleteFlag()) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        List<Attachment> attachments = attachmentService.getAttachmentsByEntity("PRODUCT", product.getId());
        List<String> urls = new ArrayList<>();
        for (Attachment attachment : attachments) {
            urls.add(attachment.getUrl());
        }
        ProductResponse response = productMapper.toDto(product);
        response.setAttachmentUrls(urls);
        List<ProcessResponse>  processResponses = product.getProductProcesses()
                .stream()
                .filter(pp -> !pp.isDeleteFlag())
                .map(pp -> {
                    Process process = pp.getProcess();
                    ProcessResponse processResponse = new ProcessResponse();
                    processResponse.setId(process.getId());
                    processResponse.setCode(process.getCode());
                    processResponse.setName(process.getName());
                    processResponse.setProductLineName(process.getProductLine().getName());
                    processResponse.setProductLineName(process.getProductLine().getCode());
                    processResponse.setProductLineId(process.getProductLine().getId());
                    return processResponse;
                })
                .toList();
        response.setProcesses(processResponses);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        log.info("Fetching all products with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Product> products = productRepository.findByDeleteFlagFalse(pageable);
        return products.map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProductsList() {
        log.info("Fetching all products (no pagination)");
        List<Product> products = productRepository.findByDeleteFlagFalse();
        return products.stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByProcessId(Long processId) {
        List<Product> products = productRepository.findByProcessId(processId);
        List<ProductResponse> responses = new ArrayList<>();
        responses = productRepository.findByProcessId(processId)
                .stream()
                .map(productMapper::toDto)
                .toList();
        for (ProductResponse productItem : responses) {
            List<Attachment> attachments = attachmentService.getAttachmentsByEntity("PRODUCT", productItem.getId());
            List<String> urls = new ArrayList<>();
            for (Attachment attachment : attachments) {
                urls.add(attachment.getUrl());
            }
            productItem.setAttachmentUrls(urls);
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByProcessIdPaginated(Long processId, Pageable pageable) {
        log.info("Fetching products for process ID: {} with pagination", processId);
        Page<Product> products = productRepository.findByProcessIdPaginated(processId, pageable);
        return products.map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String keyword) {
        log.info("Searching products with keyword: {}", keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProductsList();
        }
        List<Product> products = productRepository.searchByCodeOrName(keyword.trim());
        return products.stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Override
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.isDeleteFlag()) {
            throw new AppException(ErrorCode.PRODUCT_ALREADY_DELETED);
        }
        product.setDeleteFlag(true);
        productRepository.save(product);
        log.info("Product deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProductCodeExists(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        return productRepository.findByCodeAndNotDeleted(code.trim()).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProductCodeExistsExcludingId(String code, Long excludingId) {
        if (code == null || code.trim().isEmpty() || excludingId == null) {
            return false;
        }
        return productRepository.findByCodeAndNotDeleted(code.trim())
                .filter(product -> !product.getId().equals(excludingId))
                .isPresent();
    }
}