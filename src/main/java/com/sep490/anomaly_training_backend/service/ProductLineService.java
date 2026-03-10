package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.ProductLineRequest;
import com.sep490.anomaly_training_backend.dto.response.ProductLineResponse;

import java.util.List;

public interface ProductLineService {
    List<ProductLineResponse> getAllProductLine();

    ProductLineResponse getProductLineDetail(Long productLineId);

    ProductLineResponse createProductLine(ProductLineRequest productLineRequest);

    void deleteProductLine(Long id);

    ProductLineResponse updateProductLine(Long id, ProductLineRequest productLineRequest);

    List<ProductLineResponse> getByTeamLeadId(Long teamLeadId);
}
