package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.ProcessRequest;
import com.sep490.anomaly_training_backend.dto.request.ProductImportDto;
import com.sep490.anomaly_training_backend.dto.request.ProductRequest;
import com.sep490.anomaly_training_backend.dto.response.ProductResponse;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.mapper.ProductMapper;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.model.Product;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.ProcessRepository;
import com.sep490.anomaly_training_backend.repository.ProductLineRepository;
import com.sep490.anomaly_training_backend.repository.ProductProcessRepository;
import com.sep490.anomaly_training_backend.repository.ProductRepository;
import com.sep490.anomaly_training_backend.service.ImportHistoryService;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import com.sep490.anomaly_training_backend.service.minio.ImportImageHandlerService;
import com.sep490.anomaly_training_backend.util.helper.ProductImportHelper;
import com.sep490.anomaly_training_backend.util.validator.ProductImportValidator;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductLineRepository productLineRepository;
    @Mock private ProductMapper productMapper;
    @Mock private ImportHistoryService importHistoryService;
    @Mock private ProductImportHelper importHelper;
    @Mock private ProductImportValidator importValidator;
    @Mock private ImportImageHandlerService importImageHandlerService;
    @Mock private AttachmentService attachmentService;
    @Mock private ProductProcessRepository productProcessRepository;
    @Mock private ProcessRepository processRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private User user;
    private Product lineProduct;
    private ProductLine productLine;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testusr");

        productLine = new ProductLine();
        productLine.setId(10L);

        lineProduct = new Product();
        lineProduct.setId(100L);
        lineProduct.setCode("PRD-01");
    }

    @Test
    void getProductsByProductLineId_Success() {
        when(productLineRepository.findById(10L)).thenReturn(Optional.of(productLine));
        when(productRepository.findByProductLineIdAndDeleteFlagFalse(10L)).thenReturn(List.of(lineProduct));
        when(productMapper.toDto(lineProduct)).thenReturn(new ProductResponse());

        List<ProductResponse> responses = productService.getProductsByProductLineId(10L);

        assertThat(responses).hasSize(1);
    }

    @Test
    void createProduct_WhenNoProcesses_ShouldThrow() {
        ProductRequest req = mock(ProductRequest.class);
        Mockito.lenient().when(req.getCode()).thenReturn("PRD-02");
        Mockito.lenient().when(req.getProcesses()).thenReturn(new ArrayList<>());

        Mockito.lenient().when(productRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        Mockito.lenient().when(productRepository.save(Mockito.any(Product.class))).thenReturn(lineProduct);

        assertThatThrownBy(() -> productService.createProduct(req, user))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("ít nhất 1 công đoạn");
    }

    @Test
    void createProduct_Success() {
        ProductRequest req = mock(ProductRequest.class);
        when(req.getCode()).thenReturn("PRD-02");
        ProcessRequest pReq = new ProcessRequest();
        pReq.setId(50L);
        when(req.getProcesses()).thenReturn(List.of(pReq));

        when(productRepository.findById(any())).thenReturn(Optional.empty());
        when(productRepository.save(any())).thenReturn(lineProduct);
        
        Process process = new Process();
        process.setId(50L);
        when(processRepository.findById(50L)).thenReturn(Optional.of(process));
        
        when(productMapper.toDto(lineProduct)).thenReturn(new ProductResponse());

        ProductResponse response = productService.createProduct(req, user);

        assertThat(response).isNotNull();
        verify(productProcessRepository).save(any());
    }

    @Test
    void importProduct_Success() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("Sheet1");
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("Code");
            row.createCell(1).setCellValue("PRD-01");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);

            MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
            
            List<ProductImportDto> dtos = new ArrayList<>();
            ProductImportDto dto = new ProductImportDto();
            dto.setProductCode("PRD-01");
            dto.setProductName("Test Name");
            dto.setDescription("Desc");
            dto.setExcelRowNumber(1);
            dto.setImageData(null);
            dtos.add(dto);

            when(productLineRepository.findById(10L)).thenReturn(Optional.of(productLine));
            when(importHelper.parseExcelRows(any(), anyList())).thenReturn(dtos);
            doNothing().when(importValidator).validateFileData(anyList(), anyList());
            when(productRepository.findByCode("PRD-01")).thenReturn(Optional.of(lineProduct));
            when(productRepository.save(lineProduct)).thenReturn(lineProduct);
            
            ProductResponse response = mock(ProductResponse.class);
            when(productMapper.toDto(lineProduct)).thenReturn(response);

            List<ProductResponse> results = productService.importProduct(user, 10L, file);

            verify(importHistoryService).saveHistory(eq(user), eq("test.xlsx"), any(), argThat(s -> s.name().equals("PASS")), anyList());
        }
    }

    @Test
    void getProductById_WhenDeleted_ShouldThrow() {
        lineProduct.setDeleteFlag(true);
        when(productRepository.findById(100L)).thenReturn(Optional.of(lineProduct));

        assertThatThrownBy(() -> productService.getProductById(100L))
                .isInstanceOf(AppException.class);
    }
}
