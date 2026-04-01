package com.sep490.anomaly_training_backend.service.approval.handler;

import com.sep490.anomaly_training_backend.dto.approval.OverdueItem;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.TrainingPlanDetail;
import com.sep490.anomaly_training_backend.model.TrainingResult;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.repository.TrainingPlanRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultDetailRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingPlanApprovalHandlerTest {

    @Mock
    private TrainingResultRepository trainingResultRepository;
    @Mock
    private TrainingPlanRepository trainingPlanRepository;
    @Mock
    private TrainingResultDetailRepository trainingResultDetailRepository;

    @InjectMocks
    private TrainingPlanApprovalHandler handler;

    @Test
    void getType_ShouldReturnTrainingPlan() {
        assertThat(handler.getType()).isEqualTo(ApprovalEntityType.TRAINING_PLAN);
    }

    @Test
    void findOverdueItems_ShouldReturnItems() {
        TrainingPlan plan = new TrainingPlan();
        plan.setId(1L);
        Group g = new Group();
        g.setId(10L);
        Team t = new Team();
        t.setGroup(g);
        plan.setTeam(t);
        when(trainingPlanRepository.findByStatusAndUpdatedAtBefore(eq(ReportStatus.PENDING_APPROVAL), any(LocalDateTime.class)))
                .thenReturn(List.of(plan));

        List<OverdueItem> items = handler.findOverdueItems(ReportStatus.PENDING_APPROVAL, LocalDateTime.now());

        assertThat(items).hasSize(1);
        assertThat(items.get(0).entityId()).isEqualTo(1L);
    }

    @Test
    void applyApproval_WhenStatusCompleted_ShouldGenerateResult() {
        TrainingPlan plan = new TrainingPlan();
        plan.setId(1L);
        plan.setStatus(ReportStatus.COMPLETED);
        plan.setStartDate(LocalDate.of(2025, 1, 1));
        plan.setTitle("Plan 1");
        plan.setCreatedBy("user1");
        
        TrainingPlanDetail detail = new TrainingPlanDetail();
        detail.setId(10L);
        plan.setDetails(List.of(detail));

        when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        
        when(trainingResultRepository.save(any(TrainingResult.class))).thenAnswer(i -> {
            TrainingResult r = i.getArgument(0);
            if (r.getId() == null) r.setId(500L);
            return r;
        });

        handler.applyApproval((com.sep490.anomaly_training_backend.model.Approvable) plan);

        verify(trainingResultRepository, times(2)).save(any(TrainingResult.class));
        verify(trainingResultRepository).updateCreatedBy(500L, "user1");
        verify(trainingResultDetailRepository).updateCreatedByForResult(500L, "user1");
    }

    @Test
    void applyApproval_WhenStatusNotCompleted_ShouldThrowException() {
        TrainingPlan plan = new TrainingPlan();
        plan.setId(1L);
        plan.setStatus(ReportStatus.PENDING_REVIEW);

        when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(plan));

        assertThatThrownBy(() -> handler.applyApproval(plan))
                .isInstanceOf(AppException.class);
    }
}
