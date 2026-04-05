package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleImportDto;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleProposalDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleProposalRequest;
import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleProposalResponse;
import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleProposalUpdateResponse;
import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleResponse;
import com.sep490.anomaly_training_backend.enums.ProposalType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleMapper;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleProposalDetailMapper;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleProposalMapper;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.model.Product;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.Role;
import com.sep490.anomaly_training_backend.model.TrainingSample;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposal;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposalDetail;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.DefectService;
import com.sep490.anomaly_training_backend.service.ImportHistoryService;
import com.sep490.anomaly_training_backend.service.ProductService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import com.sep490.anomaly_training_backend.service.minio.ImportImageHandlerService;
import com.sep490.anomaly_training_backend.util.TrainingCodeGenerator;
import com.sep490.anomaly_training_backend.util.helper.TrainingSampleImportHelper;
import com.sep490.anomaly_training_backend.util.validator.TrainingSampleImportValidator;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingSampleServiceImplTest {

    @Mock private TrainingSampleRepository trainingSampleRepository;
    @Mock private ProcessRepository processRepository;
    @Mock private DefectRepository defectRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductLineRepository productLineRepository;
    @Mock private TrainingSampleMapper trainingSampleMapper;
    @Mock private ImportHistoryService importHistoryService;
    @Mock private TrainingSampleImportHelper importHelper;
    @Mock private TrainingSampleImportValidator importValidator;
    @Mock private ImportImageHandlerService importImageHandlerService;
    @Mock private AttachmentService attachmentService;
    @Mock private TrainingCodeGenerator trainingCodeGenerator;
    @Mock private ProductService productService;
    @Mock private DefectService defectService;
    @Mock private TrainingSampleProposalRepository trainingSampleProposalRepository;
    @Mock private TrainingSampleProposalDetailRepository trainingSampleProposalDetailRepository;
    @Mock private TrainingSampleProposalMapper trainingSampleProposalMapper;
    @Mock private UserRepository userRepository;
    @Mock private ApprovalService approvalService;
    @Mock private TrainingSampleProposalDetailMapper trainingSampleProposalDetailMapper;

    @Mock private HttpServletRequest httpRequest;

    @InjectMocks
    private TrainingSampleServiceImpl service;

    private User user;
    private ProductLine productLine;
    private Process process;
    private Product product;
    private TrainingSample sample;
    private TrainingSampleProposal proposal;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        Role role = new Role();
        role.setRoleCode("ROLE_TEAM_LEADER");
        user.setRoles(new HashSet<>(List.of(role)));

        productLine = new ProductLine();
        productLine.setId(10L);
        productLine.setCode("PL-01");

        process = new Process();
        process.setId(20L);

        product = new Product();
        product.setId(30L);

        sample = new TrainingSample();
        sample.setId(100L);
        sample.setTrainingCode("TS-001");
        sample.setProcess(process);
        sample.setProductLine(productLine);
        sample.setProduct(product);

        proposal = new TrainingSampleProposal();
        proposal.setId(200L);
        proposal.setProductLine(productLine);
        proposal.setStatus(ReportStatus.DRAFT);
        proposal.setCreatedBy("testuser");
    }

    @Test
    void getTrainingSampleByProductLine_Success() {
        when(trainingSampleRepository.findByProductLineIdAndDeleteFlagFalseOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(sample));
        when(trainingSampleMapper.toDto(sample)).thenReturn(mock(TrainingSampleResponse.class));
        when(attachmentService.getAttachmentsByEntity(eq("TRAINING_SAMPLE"), any())).thenReturn(List.of());

        List<TrainingSampleResponse> responses = service.getTrainingSampleByProductLine(10L);

        assertThat(responses).hasSize(1);
    }

    @Test
    void importTrainingSample_Success() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("Sheet1");
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("LineCode");
            row.createCell(1).setCellValue("PL-01");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);

            MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
            
            when(productLineRepository.findByCode("PL-01")).thenReturn(Optional.of(productLine));
            
            List<TrainingSampleImportDto> dtos = new ArrayList<>();
            TrainingSampleImportDto dto = new TrainingSampleImportDto();
            dto.setTrainingCode("TS-001");
            dto.setProcessCode("PRC-01");
            dto.setExcelRowNumber(1);
            dto.setProcessOrder(1);
            dto.setCategoryOrder(1);
            dto.setContentOrder(1);
            dto.setCategoryName("Cat");
            dto.setTrainingDescription("Desc");
            dto.setTrainingSampleCode("Code");
            dto.setNote("Note");
            dto.setImageData(null);
            
            dtos.add(dto);

            when(importHelper.parseExcelRowsWithCarryForward(any(), anyList())).thenReturn(dtos);
            doNothing().when(importValidator).validateFileData(anyList(), anyList());

            when(trainingSampleRepository.findByTrainingCode("TS-001")).thenReturn(Optional.of(sample));
            when(processRepository.findByProductLineCodeAndCode("PL-01", "PRC-01")).thenReturn(Optional.of(process));
            when(trainingSampleRepository.save(sample)).thenReturn(sample);
            when(trainingSampleMapper.toDto(sample)).thenReturn(mock(TrainingSampleResponse.class));

            List<TrainingSampleResponse> responses = service.importTrainingSample(user, file);

            assertThat(responses).hasSize(1);
            verify(attachmentService, atLeastOnce()).deleteAttachments("TRAINING_SAMPLE", 100L);
            verify(importHistoryService, atLeastOnce()).saveHistory(eq(user), eq("test.xlsx"), any(), argThat(s -> s.name().equals("PASS")), anyList());
        }
    }

    @Test
    void getTrainingSampleProposalByProductLine_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(trainingSampleProposalRepository.findByProductLineIdAndCreatedByOrderByCreatedAtDesc(10L, "testuser"))
                .thenReturn(List.of(proposal));
        when(trainingSampleProposalMapper.toResponse(proposal, userRepository)).thenReturn(mock(TrainingSampleProposalResponse.class));

        List<TrainingSampleProposalResponse> responses = service.getTrainingSampleProposalByProductLine(10L, "testuser");

        assertThat(responses).hasSize(1);
    }

    @Test
    void createTrainingSampleProposal_Success() {
        TrainingSampleProposalRequest req = mock(TrainingSampleProposalRequest.class);
        when(req.getProductLineId()).thenReturn(10L);
        
        TrainingSampleProposalDetailRequest dReq = new TrainingSampleProposalDetailRequest();
        dReq.setProcessId(20L);
        dReq.setCategoryName("Cat");
        dReq.setTrainingDescription("Desc");
        dReq.setProposalType(ProposalType.CREATE);
        
        when(req.getListDetail()).thenReturn(List.of(dReq));

        when(productLineRepository.findById(10L)).thenReturn(Optional.of(productLine));
        when(trainingSampleProposalRepository.save(any(TrainingSampleProposal.class))).thenReturn(proposal);
        when(processRepository.findById(20L)).thenReturn(Optional.of(process));
        when(trainingSampleRepository.checkExist(any(), any(), any(), any(), any())).thenReturn(Optional.empty());
        when(trainingSampleProposalDetailRepository.save(any(TrainingSampleProposalDetail.class))).thenReturn(new TrainingSampleProposalDetail());
        when(trainingSampleProposalMapper.toResponse(proposal, userRepository)).thenReturn(mock(TrainingSampleProposalResponse.class));

        TrainingSampleProposalResponse res = service.createTrainingSampleProposal(req, user);

        assertThat(res).isNotNull();
    }

    @Test
    void submit_Success() {
        when(trainingSampleProposalRepository.findById(200L)).thenReturn(Optional.of(proposal));
        
        service.submit(200L, user, httpRequest);

        verify(approvalService).submit(proposal, user, httpRequest);
        verify(trainingSampleProposalRepository).save(proposal);
    }

    @Test
    void approve_Success() {
        ApproveRequest req = new ApproveRequest();
        when(trainingSampleProposalRepository.findById(200L)).thenReturn(Optional.of(proposal));
        
        service.approve(200L, user, req, httpRequest);

        verify(approvalService).approve(proposal, user, req, httpRequest);
        verify(trainingSampleProposalRepository).save(proposal);
    }

    @Test
    void reject_Success() {
        RejectRequest req = new RejectRequest();
        when(trainingSampleProposalRepository.findById(200L)).thenReturn(Optional.of(proposal));
        
        service.reject(200L, user, req, httpRequest);

        verify(approvalService).reject(proposal, user, req, httpRequest);
        verify(trainingSampleProposalRepository).save(proposal);
    }
}
