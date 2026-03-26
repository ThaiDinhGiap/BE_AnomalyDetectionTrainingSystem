package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.ProductLineRequest;
import com.sep490.anomaly_training_backend.dto.response.ProductLineDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.ProductLineResponse;
import com.sep490.anomaly_training_backend.dto.response.WorkingPosition;
import com.sep490.anomaly_training_backend.model.User;
import org.apache.coyote.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductLineService {
    List<ProductLineResponse> getAllProductLine();

    ProductLineResponse getProductLineDetail(Long productLineId);

    ProductLineResponse createProductLine(ProductLineRequest productLineRequest);

    void deleteProductLine(Long id);

    ProductLineResponse updateProductLine(Long id, ProductLineRequest productLineRequest);

    List<ProductLineResponse> getByTeamLeadId(Long teamLeadId);

    List<ProductLineResponse> importProductLine(User user, MultipartFile productFile) throws BadRequestException;

    List<WorkingPosition> getWorkingPosition(User user);

    ProductLineDetailResponse getProductLineFullDetail(Long lineId);
}
