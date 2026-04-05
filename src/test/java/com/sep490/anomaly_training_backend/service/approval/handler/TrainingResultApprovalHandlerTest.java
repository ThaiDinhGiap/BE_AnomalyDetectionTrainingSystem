package com.sep490.anomaly_training_backend.service.approval.handler;

import com.sep490.anomaly_training_backend.dto.approval.OverdueItem;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.model.TrainingResult;
import com.sep490.anomaly_training_backend.repository.TrainingResultRepository;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.Team;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingResultApprovalHandlerTest {

    @Mock
    private TrainingResultRepository trainingResultRepository;

    @InjectMocks
    private TrainingResultApprovalHandler handler;

    @Test
    void getType_ShouldReturnTrainingResult() {
        assertThat(handler.getType()).isEqualTo(ApprovalEntityType.TRAINING_RESULT);
    }

    @Test
    void findOverdueItems_ShouldReturnFiltered() {
        TrainingResult r1 = new TrainingResult();
        Group g = new Group();
        g.setId(10L);
        Team t = new Team();
        t.setGroup(g);
        r1.setTeam(t);
        r1.setUpdatedAt(LocalDateTime.now().minusDays(2));

        when(trainingResultRepository.findByStatusAndDeleteFlagFalse(ReportStatus.PENDING_REVIEW))
                .thenReturn(List.of(r1));

        List<OverdueItem> items = handler.findOverdueItems(ReportStatus.PENDING_REVIEW, LocalDateTime.now().minusDays(1));
        
        assertThat(items).hasSize(1);
    }

    @Test
    void validateBeforeSubmit_WhenOngoing_ShouldNotThrow() {
        TrainingResult result = new TrainingResult();
        result.setStatus(ReportStatus.ONGOING);
        handler.validateBeforeSubmit(result); // Should pass cleanly
    }

    @Test
    void validateBeforeSubmit_WhenNotOngoing_ShouldThrowException() {
        TrainingResult result = new TrainingResult();
        result.setStatus(ReportStatus.DRAFT);
        
        assertThatThrownBy(() -> handler.validateBeforeSubmit(result))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Result can only be submitted when in ONGOING status");
    }

    @Test
    void afterApprove_ShouldNotBumpVersion() {
        TrainingResult result = new TrainingResult();
        result.setId(1L);
        result.setCurrentVersion(1);

        handler.afterApprove(result);

        assertThat(result.getCurrentVersion()).isEqualTo(1);
    }
}
