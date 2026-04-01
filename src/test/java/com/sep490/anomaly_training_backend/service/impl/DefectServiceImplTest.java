package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.ProductResponse;
import com.sep490.anomaly_training_backend.dto.response.defect.DefectResponse;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.mapper.DefectMapper;
import com.sep490.anomaly_training_backend.model.Defect;
import com.sep490.anomaly_training_backend.model.Product;
import com.sep490.anomaly_training_backend.repository.DefectRepository;
import com.sep490.anomaly_training_backend.service.ProductService;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefectServiceImplTest {

    @Mock
    private DefectRepository defectRepository;
    @Mock
    private DefectMapper defectMapper;
    @Mock
    private ProductService productService;
    @Mock
    private AttachmentService attachmentService;

    @InjectMocks
    private DefectServiceImpl defectService;

    @Test
    void getDefectById_ShouldReturnResponse() {
        Product product = new Product();
        product.setId(100L);

        Defect defect = new Defect();
        defect.setId(20L);
        defect.setProduct(product);
        defect.setDefectDescription("Defect A");

        DefectResponse responseMock = DefectResponse.builder()
            .defectId(20L)
            .defectDescription("Defect A")
            .build();

        ProductResponse productResp = new ProductResponse();
        productResp.setId(100L);

        when(defectRepository.findById(20L)).thenReturn(Optional.of(defect));
        when(defectMapper.toDto(any(Defect.class))).thenReturn(responseMock);
        when(productService.getProductById(100L)).thenReturn(productResp);
        when(attachmentService.getAttachmentsByEntity(eq("DEFECT"), eq(20L))).thenReturn(List.of());

        DefectResponse response = defectService.getDefectById(20L);

        assertThat(response.getDefectId()).isEqualTo(20L);
        assertThat(response.getDefectDescription()).isEqualTo("Defect A");
        assertThat(response.getProduct()).isNotNull();
    }

    @Test
    void getDefectById_WhenNotFound_ShouldThrow() {
        when(defectRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> defectService.getDefectById(20L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Defect not found"); // Actually ErrorCode.DEFECT_NOT_FOUND
    }

    @Test
    void checkExistDefectDescription_ShouldReturnBoolean() {
        when(defectRepository.existsActiveByDefectDescriptionIgnoreCase("Desc")).thenReturn(true);

        Boolean result = defectService.checkExistDefectDescription("Desc");

        assertThat(result).isTrue();
    }
}
