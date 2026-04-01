package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.ProductRequest;
import com.sep490.anomaly_training_backend.dto.response.ProductResponse;
import com.sep490.anomaly_training_backend.model.User;
import org.apache.coyote.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    ProductResponse getProductById(Long id);

//    Page<ProductResponse> getAllProducts(Pageable pageable);

    List<ProductResponse> getAllProductsList();

    List<ProductResponse> getProductsByProcessId(Long processId);

//    Page<ProductResponse> getProductsByProcessIdPaginated(Long processId, Pageable pageable);
//
//    List<ProductResponse> searchProducts(String keyword);
//
//    void deleteProduct(Long id);
//
//    boolean isProductCodeExists(String code);
//
//    boolean isProductCodeExistsExcludingId(String code, Long excludingId);

    List<ProductResponse> importProduct(User user, Long productLineId, MultipartFile productFile) throws BadRequestException;

    List<ProductResponse> getProductsByProductLineId(Long productLineId);

    ProductResponse createProduct(ProductRequest productRequest, User currentUser);

    List<ProductResponse> syncProduct(List<ProductRequest> productRequestList, User currentUser);


}

