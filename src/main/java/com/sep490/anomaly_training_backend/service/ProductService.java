package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.response.ProductResponse;
import com.sep490.anomaly_training_backend.model.User;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {


    /**
     * Get product by ID
     */
    ProductResponse getProductById(Long id);

    /**
     * Get all products with pagination
     */
    Page<ProductResponse> getAllProducts(Pageable pageable);

    /**
     * Get all products without pagination
     */
    List<ProductResponse> getAllProductsList();

    /**
     * Get all products by process ID
     */
    List<ProductResponse> getProductsByProcessId(Long processId);

    /**
     * Get all products by process ID with pagination
     */
    Page<ProductResponse> getProductsByProcessIdPaginated(Long processId, Pageable pageable);

    /**
     * Search products by code or name
     */
    List<ProductResponse> searchProducts(String keyword);

    /**
     * Soft delete product (set deleteFlag = true)
     */
    void deleteProduct(Long id);

    /**
     * Check if product code exists
     */
    boolean isProductCodeExists(String code);

    /**
     * Check if product code exists (excluding a specific ID for update validation)
     */
    boolean isProductCodeExistsExcludingId(String code, Long excludingId);

    List<ProductResponse> importProduct(User user, Long productLineId, MultipartFile productFile) throws BadRequestException;

    List<ProductResponse> getProductsByProductLineId(Long productLineId);
}

