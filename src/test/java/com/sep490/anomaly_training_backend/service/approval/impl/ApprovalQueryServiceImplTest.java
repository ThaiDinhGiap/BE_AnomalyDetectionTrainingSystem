package com.sep490.anomaly_training_backend.service.approval.impl;

import com.sep490.anomaly_training_backend.dto.approval.ApprovalHistoryResponse;
import com.sep490.anomaly_training_backend.enums.ApprovalAction;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.model.ApprovalActionLog;
import com.sep490.anomaly_training_backend.model.ApprovalFlowStep;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.approval.ApprovalRouteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApprovalQueryServiceImplTest {

    @Mock
    private DefectProposalRepository defectProposalRepository;
    @Mock
    private TrainingSampleProposalRepository trainingSampleProposalRepository;
    @Mock
    private TrainingPlanRepository planRepo;
    @Mock
    private ApprovalActionRepository actionRepo;
    @Mock
    private ApprovalFlowStepRepository flowStepRepo;
    @Mock
    private RejectReasonRepository rejectReasonRepo;
    @Mock
    private RequiredActionRepository requiredActionRepo;
    @Mock
    private TrainingSampleReviewRepository trainingSampleReviewRepository;
    @Mock
    private ApprovalRouteService approvalRouteService;

    @InjectMocks
    private ApprovalQueryServiceImpl queryService;

    private User user;
    private ApprovalActionLog actionLog;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(100L);
        user.setUsername("user1");

        actionLog = ApprovalActionLog.builder()
                .id(10L)
                .entityType(ApprovalEntityType.DEFECT_PROPOSAL)
                .entityId(1L)
                .entityVersion(1)
                .stepOrder(1)
                .action(ApprovalAction.APPROVE)
                .performedByUsername("user1")
                .performedAt(Instant.now())
                .build();
    }

    @Test
    void getApprovalHistory_ShouldReturnLogs() {
        when(actionRepo.findByEntityTypeAndEntityIdOrderByPerformedAtAsc(ApprovalEntityType.DEFECT_PROPOSAL, 1L))
                .thenReturn(List.of(actionLog));

        List<ApprovalHistoryResponse> history = queryService.getApprovalHistory(ApprovalEntityType.DEFECT_PROPOSAL, 1L);

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getId()).isEqualTo(10L);
        assertThat(history.get(0).getAction()).isEqualTo(ApprovalAction.APPROVE);
    }

    @Test
    void getApprovalHistoryByVersion_ShouldReturnLogsForVersion() {
        when(actionRepo.findByEntityTypeAndEntityIdAndEntityVersionOrderByPerformedAtAsc(ApprovalEntityType.DEFECT_PROPOSAL, 1L, 1))
                .thenReturn(List.of(actionLog));

        List<ApprovalHistoryResponse> history = queryService.getApprovalHistoryByVersion(ApprovalEntityType.DEFECT_PROPOSAL, 1L, 1);

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getEntityVersion()).isEqualTo(1);
    }
}
