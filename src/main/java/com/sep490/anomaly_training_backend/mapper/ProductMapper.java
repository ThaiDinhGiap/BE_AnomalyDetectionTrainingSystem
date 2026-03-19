package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.response.ProductResponse;
import com.sep490.anomaly_training_backend.model.Product;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring", uses = {ProcessMapper.class})
public interface ProductMapper {
    ProductResponse toDto(Product entity);
}

