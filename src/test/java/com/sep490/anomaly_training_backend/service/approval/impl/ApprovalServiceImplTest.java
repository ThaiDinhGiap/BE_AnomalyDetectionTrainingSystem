package com.sep490.anomaly_training_backend.service.approval.impl;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.enums.ApprovalAction;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.model.ApprovalFlowStep;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.RejectReason;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.model.RequiredAction;
import com.sep490.anomaly_training_backend.model.Role;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.ApprovalActionRepository;
import com.sep490.anomaly_training_backend.repository.ApprovalFlowStepRepository;
import com.sep490.anomaly_training_backend.repository.RejectReasonRepository;
import com.sep490.anomaly_training_backend.repository.RequiredActionRepository;
import com.sep490.anomaly_training_backend.service.approval.ApprovalHandler;
import com.sep490.anomaly_training_backend.service.approval.ApprovalRouteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceImplTest {

    @Mock
    private ApprovalFlowStepRepository flowStepRepo;
    @Mock
    private ApprovalActionRepository actionRepo;
    @Mock
    private ApprovalRouteService routeService;
    @Mock
    private RejectReasonRepository rejectReasonRepo;
    @Mock
    private RequiredActionRepository requiredActionRepo;
    @Mock
    private ApprovalHandlerRegistry handlerRegistry;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ApprovalServiceImpl approvalService;

    private User user;
    private TrainingPlan plan;
    private ApprovalHandler handler;
    private ApprovalFlowStep step1;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(100L);
        user.setUsername("user_foo");
        Role r = new Role();
        r.setRoleCode("ROLE_USER");
        user.setRoles(Set.of(r));

        plan = new TrainingPlan();
        plan.setId(10L);
        Group g = new Group();
        g.setId(5L);
        Team t = new Team();
        t.setGroup(g);
        plan.setTeam(t);
        plan.setCurrentVersion(1);
        plan.setStatus(ReportStatus.DRAFT);

        handler = mock(ApprovalHandler.class);
        lenient().when(handlerRegistry.getHandler(any())).thenReturn(handler);

        step1 = new ApprovalFlowStep();
        step1.setStepOrder(1);
        step1.setIsActive(true);
        step1.setRequiredPermission("APPROVE_PLAN");
        step1.setPendingStatus(ReportStatus.PENDING_REVIEW);
    }

    @Test
    void submit_ShouldLogActionAndChangeStatus() {
        when(handler.requiresFlowStepOnSubmit()).thenReturn(true);
        when(flowStepRepo.findByEntityTypeAndIsActiveTrueOrderByStepOrderAsc(any()))
                .thenReturn(List.of(step1));

        approvalService.submit(plan, user, null);

        verify(handler).validateBeforeSubmit(plan);
        verify(handler).prepareForSubmit(plan);
        assertThat(plan.getStatus()).isEqualTo(ReportStatus.PENDING_REVIEW);
        verify(actionRepo).save(any());
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void revise_WhenNotRejected_ShouldThrow() {
        plan.setStatus(ReportStatus.PENDING_REVIEW);
        
        assertThatThrownBy(() -> approvalService.revise(plan, user, null))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("can only be revised when in REJECTED status");
    }

    @Test
    void revise_ShouldUpdateStatusAndLog() {
        plan.setStatus(ReportStatus.REJECTED);
        
        approvalService.revise(plan, user, null);

        assertThat(plan.getStatus()).isEqualTo(ReportStatus.REVISING);
        assertThat(plan.getCurrentVersion()).isEqualTo(2);
        verify(actionRepo).save(any());
    }
}
