package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.ProductResponse;
import com.sep490.anomaly_training_backend.mapper.ProductMapper;
import com.sep490.anomaly_training_backend.model.Product;
import com.sep490.anomaly_training_backend.repository.ProductRepository;
import com.sep490.anomaly_training_backend.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;


    /**
     * Get product by ID (only if not deleted)
     */
    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.info("Fetching product with ID: {}", id);

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + id));

        if (product.isDeleteFlag()) {
            throw new EntityNotFoundException("Product not found with ID: " + id);
        }

        return productMapper.toDto(product);
    }

    /**
     * Get all products with pagination
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        log.info("Fetching all products with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Product> products = productRepository.findByDeleteFlagFalse(pageable);
        return products.map(productMapper::toDto);
    }

    /**
     * Get all products without pagination
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProductsList() {
        log.info("Fetching all products (no pagination)");

        List<Product> products = productRepository.findByDeleteFlagFalse();
        return products.stream()
            .map(productMapper::toDto)
            .toList();
    }

    /**
     * Get all products by process ID
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByProcessId(Long processId) {
        log.info("Fetching products for process ID: {}", processId);

        List<Product> products = productRepository.findByProcessId(processId);
        return products.stream()
            .map(productMapper::toDto)
            .toList();
    }

    /**
     * Get all products by process ID with pagination
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByProcessIdPaginated(Long processId, Pageable pageable) {
        log.info("Fetching products for process ID: {} with pagination", processId);

        Page<Product> products = productRepository.findByProcessIdPaginated(processId, pageable);
        return products.map(productMapper::toDto);
    }

    /**
     * Search products by code or name
     */
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

    /**
     * Soft delete product (set deleteFlag = true)
     */
    @Override
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + id));

        if (product.isDeleteFlag()) {
            throw new IllegalArgumentException("Product is already deleted");
        }

        product.setDeleteFlag(true);
        productRepository.save(product);
        log.info("Product deleted successfully with ID: {}", id);
    }

    /**
     * Check if product code exists (among non-deleted products)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isProductCodeExists(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }

        return productRepository.findByCodeAndNotDeleted(code.trim())
            .isPresent();
    }

    /**
     * Check if product code exists excluding a specific ID
     * Used for validation during update
     */
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

