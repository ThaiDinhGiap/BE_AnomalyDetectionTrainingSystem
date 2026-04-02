package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.UpdateTrainingResultRequest;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.TrainingPlanDetail;
import com.sep490.anomaly_training_backend.model.TrainingResult;
import com.sep490.anomaly_training_backend.model.TrainingResultDetail;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingResultServiceImplTest {

    @Mock private TrainingResultRepository trainingResultRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private ProcessRepository processRepository;
    @Mock private UserRepository userRepository;
    @Mock private TrainingPlanRepository trainingPlanRepository;
    @Mock private ProductLineRepository productLineRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private RejectReasonRepository rejectReasonRepository;
    @Mock private ApprovalService approvalService;
    @Mock private GroupRepository groupRepository;
    @Mock private EmployeeSkillRepository employeeSkillRepository;
    @Mock private TrainingSampleRepository trainingSampleRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductProcessRepository productProcessRepository;
    @Mock private TrainingResultDetailRepository trainingResultDetailRepository;
    @Mock private PrioritySnapshotRepository prioritySnapshotRepository;
    @Mock private RequiredActionRepository requiredActionRepository;

    @Mock private HttpServletRequest httpRequest;

    @InjectMocks
    private TrainingResultServiceImpl service;

    private User user;
    private TrainingResult result;
    private TrainingResultDetail detail;
    private ProductLine productLine;
    private Team team;
    private Employee employee;
    private Process process;
    private TrainingPlanDetail planDetail;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        productLine = new ProductLine();
        productLine.setId(10L);
        productLine.setName("PL-Name");
        productLine.setCode("PL-CODE");

        team = new Team();
        team.setId(20L);

        employee = new Employee();
        employee.setId(30L);

        process = new Process();
        process.setId(40L);

        TrainingPlan plan = new TrainingPlan();
        plan.setId(100L);
        plan.setLine(productLine);

        planDetail = new TrainingPlanDetail();
        planDetail.setId(50L);
        planDetail.setEmployee(employee);
        planDetail.setTrainingPlan(plan);

        result = new TrainingResult();
        result.setId(200L);
        result.setLine(productLine);
        result.setTeam(team);
        result.setStatus(ReportStatus.DRAFT);
        result.setCreatedBy("testuser");
        result.setTrainingPlan(plan);

        detail = new TrainingResultDetail();
        detail.setId(300L);
        detail.setTrainingResult(result);
        detail.setEmployee(employee);
        detail.setProcess(process);
        detail.setTrainingPlanDetail(planDetail);
        
        result.setDetails(List.of(detail));
    }

    @Test
    void submitResultForApproval_Success() {
        productLine.setName("PL-Name");
        productLine.setCode("PL");
        result.setStatus(ReportStatus.ONGOING);
        when(trainingResultRepository.findByIdWithDetails(200L)).thenReturn(Optional.of(result));
        when(trainingResultRepository.save(result)).thenReturn(result);
        when(trainingResultDetailRepository.findPendingWithIsPassByResultId(200L))
                .thenReturn(Collections.emptyList());

        service.submit(200L, user, httpRequest);

        assertThat(result.getStatus()).isEqualTo(ReportStatus.PENDING_REVIEW);
        verify(trainingResultRepository).save(result);
        verify(approvalService, never()).submit(any(), any(), any());
    }

    @Test
    void approveResult_Success_UpdatesSkills() {
        ApproveRequest req = new ApproveRequest();
        
        when(trainingResultRepository.findByIdWithDetails(200L)).thenReturn(Optional.of(result));
        when(trainingResultRepository.save(result)).thenReturn(result);

        service.approve(200L, user, req, httpRequest);

        verify(approvalService).approve(result, user, req, httpRequest);
        verify(trainingResultRepository).save(result);
    }

    @Test
    void rejectResult_Success() {
        productLine.setName("PL-Name");
        RejectRequest req = new RejectRequest();
        
        when(trainingResultRepository.findByIdWithDetails(200L)).thenReturn(Optional.of(result));

        service.reject(200L, user, req, httpRequest);

        verify(approvalService).reject(result, user, req, httpRequest);
    }

    @Test
    void updateResult_Success() {
        org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        org.springframework.security.core.context.SecurityContext ctx = mock(org.springframework.security.core.context.SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(ctx);

        UpdateTrainingResultRequest req = new UpdateTrainingResultRequest();
        req.setId(200L);
        com.sep490.anomaly_training_backend.dto.request.UpdateResultDetailRequest dReq = new com.sep490.anomaly_training_backend.dto.request.UpdateResultDetailRequest();
        dReq.setId(300L);
        dReq.setIsPass(true);
        req.setDetails(List.of(dReq));

        when(trainingResultDetailRepository.findById(any())).thenReturn(Optional.of(detail));
        when(trainingResultRepository.save(result)).thenReturn(result);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(trainingResultRepository.findById(200L)).thenReturn(Optional.of(result));

        service.updateResult(req);

        assertThat(detail.getIsPass()).isTrue();
        verify(trainingResultDetailRepository).saveAll(anyList());
        
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }
}
