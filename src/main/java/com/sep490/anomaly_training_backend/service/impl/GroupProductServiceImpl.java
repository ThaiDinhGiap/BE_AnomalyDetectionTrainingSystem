package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.GroupProductRequest;
import com.sep490.anomaly_training_backend.dto.response.GroupProductResponse;
import com.sep490.anomaly_training_backend.model.GroupProduct;
import com.sep490.anomaly_training_backend.mapper.GroupProductMapper;
import com.sep490.anomaly_training_backend.repository.GroupProductRepository;
import com.sep490.anomaly_training_backend.service.GroupProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupProductServiceImpl implements GroupProductService {

    private final GroupProductRepository productRepository;
    private final GroupProductMapper productMapper;

    @Override
    @Transactional
    public GroupProductResponse createProduct(GroupProductRequest request) {
        // Check Unique: Trong Group này đã có mã sản phẩm này chưa?
        if (productRepository.existsByGroupIdAndProductCode(request.getGroupId(), request.getProductCode())) {
            throw new RuntimeException("Mã sản phẩm '" + request.getProductCode() + "' đã tồn tại trong nhóm này.");
        }

        GroupProduct entity = productMapper.toEntity(request);
        return productMapper.toDTO(productRepository.save(entity));
    }

    @Override
    @Transactional
    public GroupProductResponse updateProduct(Long id, GroupProductRequest request) {
        GroupProduct entity = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found id: " + id));

        // Nếu thay đổi Group hoặc ProductCode, phải check trùng lặp (trừ chính bản ghi này ra)
        boolean isGroupChanged = !entity.getGroup().getId().equals(request.getGroupId());
        boolean isCodeChanged = !entity.getProductCode().equals(request.getProductCode());

        if (isGroupChanged || isCodeChanged) {
            if (productRepository.existsByGroupIdAndProductCode(request.getGroupId(), request.getProductCode())) {
                throw new RuntimeException("Mã sản phẩm '" + request.getProductCode() + "' đã tồn tại trong nhóm này.");
            }
        }

        productMapper.updateEntity(entity, request);
        return productMapper.toDTO(productRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        GroupProduct entity = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found id: " + id));

        // Soft Delete (Vì class extend BaseEntity)
        entity.setDeleteFlag(true);
        productRepository.save(entity);
    }

    @Override
    public GroupProductResponse getProductById(Long id) {
        return productRepository.findById(id)
                .filter(p -> !p.isDeleteFlag())
                .map(productMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Product not found or deleted"));
    }

    @Override
    public List<GroupProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .filter(p -> !p.isDeleteFlag())
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupProductResponse> getProductsByGroup(Long groupId) {
        return productRepository.findByGroupId(groupId).stream()
                .filter(p -> !p.isDeleteFlag())
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }
}