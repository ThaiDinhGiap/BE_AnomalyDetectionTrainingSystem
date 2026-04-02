package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.ScheduleRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanGenerationRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanUpdateRequest;
import com.sep490.anomaly_training_backend.dto.response.PrioritizedEmployeeResponse;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.dto.response.ProductLineResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanGenerationResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanResponse;
import com.sep490.anomaly_training_backend.dto.scoring.PrioritySnapshotResponse;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.EmployeeStatus;
import com.sep490.anomaly_training_backend.enums.PolicyEntityType;
import com.sep490.anomaly_training_backend.enums.PolicyStatus;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.PrioritySnapshotMapper;
import com.sep490.anomaly_training_backend.mapper.TrainingPlanMapper;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.PriorityPolicy;
import com.sep490.anomaly_training_backend.model.PrioritySnapshot;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.TrainingPlanDetail;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import com.sep490.anomaly_training_backend.service.priority.TrainingPlanScheduleGenerationService;
import com.sep490.anomaly_training_backend.service.priority.impl.PriorityScoringServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanUpdateRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingPlanServiceImplTest {

    @Mock private TrainingPlanRepository trainingPlanRepository;
    @Mock private TrainingPlanDetailRepository trainingPlanDetailRepository;
    @Mock private TrainingPlanMapper planMapper;
    @Mock private PrioritySnapshotMapper prioritySnapshotMapper;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private ProcessRepository processRepository;
    @Mock private UserRepository userRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private TrainingPlanHistoryRepository trainingPlanHistoryRepository;
    @Mock private ApprovalService approvalService;
    @Mock private ProductLineRepository productLineRepository;
    @Mock private EmployeeSkillRepository employeeSkillRepository;
    @Mock private TrainingResultDetailRepository trainingResultDetailRepository;
    @Mock private TrainingResultRepository trainingResultRepository;
    @Mock private PriorityScoringServiceImpl priorityScoringService;
    @Mock private PriorityPolicyRepository policyRepository;
    @Mock private PrioritySnapshotRepository prioritySnapshotRepository;
    @Mock private PrioritySnapshotDetailRepository prioritySnapshotDetailRepository;
    @Mock private TrainingPlanScheduleGenerationService trainingPlanScheduleGenerationService;
    @Mock private GroupRepository groupRepository;
    @Mock private RejectReasonRepository rejectReasonRepository;
    @Mock private RequiredActionRepository requiredActionRepository;
    @Mock private TrainingPlanSpecialDayRepository trainingPlanSpecialDayRepository;

    @Mock private HttpServletRequest httpRequest;

    @InjectMocks
    private TrainingPlanServiceImpl service;

    private User user;
    private TrainingPlan plan;
    private TrainingPlanDetail detail;
    private Team team;
    private Group group;
    private ProductLine productLine;
    private Employee employee;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        group = new Group();
        group.setId(10L);

        team = new Team();
        team.setId(20L);
        team.setGroup(group);

        productLine = new ProductLine();
        productLine.setId(30L);
        productLine.setCode("PL-1");

        plan = new TrainingPlan();
        plan.setId(100L);
        plan.setTitle("Plan 1");
        plan.setTeam(team);
        plan.setLine(productLine);
        plan.setStatus(ReportStatus.DRAFT);
        plan.setCreatedBy("testuser");

        employee = new Employee();
        employee.setId(50L);

        detail = new TrainingPlanDetail();
        detail.setId(200L);
        detail.setTrainingPlan(plan);
        detail.setEmployee(employee);
        
        plan.setDetails(new ArrayList<>(List.of(detail)));
    }

    @Test
    void getPlanDetail_Success() {
        when(trainingPlanRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(plan));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        
        TrainingPlanResponse planResp = new TrainingPlanResponse();
        planResp.setDetails(new ArrayList<>());
        when(planMapper.toResponse(plan)).thenReturn(planResp);

        TrainingPlanGenerationResponse res = service.getPlanDetail(100L);

        assertThat(res).isNotNull();
    }

    @Test
    void getAllPlans_ByCreator_Success() {
        when(trainingPlanRepository.findByCreatedByAndLineIdAndDeleteFlagFalse("testuser", 30L))
                .thenReturn(List.of(plan));
                
        TrainingPlanResponse planResp = new TrainingPlanResponse();
        planResp.setDetails(new ArrayList<>());
        when(planMapper.toResponse(plan)).thenReturn(planResp);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        List<TrainingPlanGenerationResponse> res = service.getAllPlans(user, 30L);

        assertThat(res).hasSize(1);
    }

    @Test
    void updatePlan_Success() {
        TrainingPlanUpdateRequest req = new TrainingPlanUpdateRequest();
        req.setTitle("Updated Plan");
        req.setLineId(30L);

        TrainingPlanUpdateRequest.DetailAction action = new TrainingPlanUpdateRequest.DetailAction();
        action.setAction(TrainingPlanUpdateRequest.ActionType.UPDATE);
        action.setDetailId(200L);
        action.setEmployeeId(50L);
        req.setDetails(List.of(action));

        when(trainingPlanRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(plan));
        when(productLineRepository.findById(30L)).thenReturn(Optional.of(productLine));
        when(employeeRepository.findById(50L)).thenReturn(Optional.of(employee));
        when(trainingPlanRepository.save(plan)).thenReturn(plan);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        
        TrainingPlanResponse planResp = new TrainingPlanResponse();
        planResp.setDetails(new ArrayList<>());
        when(planMapper.toResponse(plan)).thenReturn(planResp);

        TrainingPlanGenerationResponse res = service.updatePlan(100L, req);

        assertThat(res).isNotNull();
    }

    @Test
    void updatePlan_CannotUpdateCompletedDetail() {
        detail.setStatus(ReportStatus.COMPLETED);
        plan.setStatus(ReportStatus.COMPLETED);

        TrainingPlanUpdateRequest req = new TrainingPlanUpdateRequest();
        TrainingPlanUpdateRequest.DetailAction action = new TrainingPlanUpdateRequest.DetailAction();
        action.setAction(TrainingPlanUpdateRequest.ActionType.UPDATE);
        action.setDetailId(200L);
        action.setNote("test");
        req.setDetails(List.of(action));

        when(trainingPlanRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(plan));

        assertThatThrownBy(() -> service.updatePlan(100L, req))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CANNOT_UPDATE_COMPLETED_DETAIL);
    }

    @Test
    void generateTrainingPlans_Success() {
        TrainingPlanGenerationRequest req = new TrainingPlanGenerationRequest();
        req.setTeamId(20L);
        req.setLineId(30L);
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now().plusMonths(1));

        PriorityPolicy policy = new PriorityPolicy();
        policy.setId(5L);

        PrioritySnapshot snapshot = new PrioritySnapshot();
        snapshot.setId(8L);

        when(productLineRepository.findById(30L)).thenReturn(Optional.of(productLine));
        when(teamRepository.findById(20L)).thenReturn(Optional.of(team));
        when(employeeRepository.findAllActiveByTeamId(20L, EmployeeStatus.ACTIVE)).thenReturn(List.of(employee));
        when(policyRepository.findFirstByEntityTypeAndStatusAndDeleteFlagFalse(PolicyEntityType.EMPLOYEE, PolicyStatus.ACTIVE))
                .thenReturn(Optional.of(policy));
        when(priorityScoringService.generateSnapshot(anyLong(), anyLong(), anyList())).thenReturn(snapshot);
        when(trainingPlanScheduleGenerationService.generateOptimalSchedule(any(), any(), anyInt())).thenReturn(plan);
        when(prioritySnapshotRepository.findById(8L)).thenReturn(Optional.of(snapshot));

        when(trainingPlanRepository.save(any(TrainingPlan.class))).thenReturn(plan);
        when(planMapper.toResponse(any())).thenReturn(new TrainingPlanResponse());

        TrainingPlanGenerationResponse res = service.generateTrainingPlans(user, req);

        assertThat(res).isNotNull();
        verify(prioritySnapshotRepository, times(1)).save(snapshot);
    }

    @Test
    void submitPlanForApproval_Success() {
        plan.setStartDate(LocalDate.now());
        plan.setEndDate(LocalDate.now().plusMonths(1));
        detail.setPlannedDate(LocalDate.now());
        when(trainingPlanRepository.findById(100L)).thenReturn(Optional.of(plan));

        service.submitPlanForApproval(100L, user, httpRequest);

        verify(approvalService).submit(plan, user, httpRequest);
        assertThat(plan.getFormCode()).startsWith("TR_PLAN_PL");
    }

    @Test
    void approve_Success() {
        ApproveRequest req = new ApproveRequest();
        when(trainingPlanRepository.findById(100L)).thenReturn(Optional.of(plan));

        service.approve(100L, user, req, httpRequest);

        verify(approvalService).approve(plan, user, req, httpRequest);
        verify(trainingPlanRepository).save(plan);
    }
}
