package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.ProductLineImportDto;
import com.sep490.anomaly_training_backend.dto.request.ProductLineRequest;
import com.sep490.anomaly_training_backend.dto.response.OrgDropdownItem;
import com.sep490.anomaly_training_backend.dto.response.ProductLineDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.ProductLineResponse;
import com.sep490.anomaly_training_backend.dto.response.WorkingPosition;
import com.sep490.anomaly_training_backend.enums.OrgHierarchyLevel;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.mapper.EmployeeSkillMapper;
import com.sep490.anomaly_training_backend.mapper.ProcessMapper;
import com.sep490.anomaly_training_backend.mapper.ProductLineMapper;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.ImportHistoryService;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import com.sep490.anomaly_training_backend.util.helper.ProductLineImportHelper;
import com.sep490.anomaly_training_backend.util.validator.ProductLineImportValidator;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductLineServiceImplTest {

    @Mock private ProductLineRepository productLineRepository;
    @Mock private ProcessRepository processRepository;
    @Mock private ProductLineMapper productLineMapper;
    @Mock private EmployeeSkillRepository employeeSkillRepository;
    @Mock private EmployeeSkillMapper employeeSkillMapper;
    @Mock private GroupRepository groupRepository;
    @Mock private ImportHistoryService importHistoryService;
    @Mock private ProductLineImportHelper importHelper;
    @Mock private ProductLineImportValidator importValidator;
    @Mock private ProcessMapper processMapper;
    @Mock private TeamRepository teamRepository;
    @Mock private SectionRepository sectionRepository;
    @Mock private ProductProcessRepository productProcessRepository;
    @Mock private AttachmentService attachmentService;

    @InjectMocks
    private ProductLineServiceImpl productLineService;

    private User user;
    private ProductLine productLine;
    private Group group;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        group = new Group();
        group.setId(10L);

        productLine = new ProductLine();
        productLine.setId(100L);
        productLine.setCode("PL-01");
        productLine.setName("Product Line 1");
        productLine.setGroup(group);
    }

    @Test
    void getAllProductLine_ShouldReturnListWithSkills() {
        when(productLineRepository.findByDeleteFlagFalse()).thenReturn(List.of(productLine));
        
        ProductLineResponse plResp = mock(ProductLineResponse.class);
        when(plResp.getId()).thenReturn(100L);
        when(productLineMapper.toDto(productLine)).thenReturn(plResp);

        List<ProductLineResponse> result = productLineService.getAllProductLine();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(100L);
    }

    @Test
    void createProductLine_WhenCodeExists_ShouldThrow() {
        ProductLineRequest req = mock(ProductLineRequest.class);
        when(req.getGroupId()).thenReturn(10L);
        when(req.getCode()).thenReturn("PL-01");

        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(productLineRepository.findByCode("PL-01")).thenReturn(Optional.of(productLine));

        assertThatThrownBy(() -> productLineService.createProductLine(req))
                .isInstanceOf(AppException.class);
    }

    @Test
    void createProductLine_Success() {
        ProductLineRequest req = mock(ProductLineRequest.class);
        when(req.getGroupId()).thenReturn(10L);
        when(req.getCode()).thenReturn("PL-02");

        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(productLineRepository.findByCode("PL-02")).thenReturn(Optional.empty());
        when(productLineRepository.save(any(ProductLine.class))).thenReturn(productLine);
        when(productLineMapper.toDto(productLine)).thenReturn(mock(ProductLineResponse.class));

        ProductLineResponse result = productLineService.createProductLine(req);

        assertThat(result).isNotNull();
        verify(productLineRepository).save(any(ProductLine.class));
    }

    @Test
    void deleteProductLine_Success() {
        when(productLineRepository.findById(100L)).thenReturn(Optional.of(productLine));
        
        productLineService.deleteProductLine(100L);
        
        assertThat(productLine.isDeleteFlag()).isTrue();
        verify(productLineRepository).saveAndFlush(productLine);
    }

    @Test
    void importProductLine_WhenFileEmpty_ShouldThrow() {
        MockMultipartFile file = new MockMultipartFile("file", new byte[0]);
        
        assertThatThrownBy(() -> productLineService.importProductLine(user, file))
                .isInstanceOf(AppException.class);
    }

    @Test
    void importProductLine_Success() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            wb.createSheet("Sheet1");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);

            MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
            
            List<ProductLineImportDto> dtos = new ArrayList<>();
            ProductLineImportDto dto = new ProductLineImportDto();
            dto.setProductLineCode("PL-01");
            dto.setProcessCode("PRC-01");
            dtos.add(dto);

            when(importHelper.parseExcelRows(any(), anyList())).thenReturn(dtos);
            doNothing().when(importValidator).validateFileData(anyList(), anyList());
            when(productLineRepository.findByCode("PL-01")).thenReturn(Optional.of(productLine));
            when(productLineRepository.save(productLine)).thenReturn(productLine);

            Process process = new Process();
            when(processRepository.findByProductLineCodeAndCode("PL-01", "PRC-01")).thenReturn(Optional.of(process));
            when(processRepository.save(process)).thenReturn(process);

            when(productLineMapper.toDto(productLine)).thenReturn(mock(ProductLineResponse.class));

            List<ProductLineResponse> responses = productLineService.importProductLine(user, file);

            assertThat(responses).hasSize(1);
            verify(importHistoryService).saveHistory(eq(user), eq("test.xlsx"), any(), argThat(s -> s.name().equals("PASS")), anyList());
        }
    }

    @Test
    void getProductLineFullDetail_Success() {
        when(productLineRepository.findById(100L)).thenReturn(Optional.of(productLine));
        
        List<Process> processes = List.of(new Process());
        when(processRepository.findByProductLineIdAndDeleteFlagFalse(100L)).thenReturn(processes);

        ProductLineDetailResponse response = productLineService.getProductLineFullDetail(100L);
        
        assertThat(response).isNotNull();
        assertThat(response.getLineId()).isEqualTo(100L);
    }

    @Test
    void getOrgHierarchy_ShouldReturnHierarchyLevel() {
        when(productLineRepository.findByGroupIdAndDeleteFlagFalse(10L)).thenReturn(List.of(productLine));
        
        List<OrgDropdownItem> items = productLineService.getOrgHierarchy(user, OrgHierarchyLevel.PRODUCT_LINE, null, 10L);
        
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getId()).isEqualTo(100L);
    }
}
