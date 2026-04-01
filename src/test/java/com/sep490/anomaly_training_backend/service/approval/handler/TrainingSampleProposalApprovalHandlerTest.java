package com.sep490.anomaly_training_backend.service.approval.handler;

import com.sep490.anomaly_training_backend.dto.approval.OverdueItem;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ProposalType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.TrainingSample;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposal;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposalDetail;
import com.sep490.anomaly_training_backend.repository.AttachmentRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleProposalDetailRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleProposalRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleRepository;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import com.sep490.anomaly_training_backend.util.TrainingCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingSampleProposalApprovalHandlerTest {

    @Mock
    private TrainingSampleRepository trainingSampleRepository;
    @Mock
    private TrainingCodeGenerator trainingCodeGenerator;
    @Mock
    private AttachmentService attachmentService;
    @Mock
    private TrainingSampleProposalDetailRepository proposalDetailRepository;
    @Mock
    private AttachmentRepository attachmentRepository;
    @Mock
    private TrainingSampleProposalRepository trainingSampleProposalRepository;

    @InjectMocks
    private TrainingSampleProposalApprovalHandler handler;

    private TrainingSampleProposal proposal;
    private Process process;

    @BeforeEach
    void setUp() {
        proposal = new TrainingSampleProposal();
        proposal.setId(1L);
        Group g = new Group();
        g.setId(5L);
        ProductLine pl = new ProductLine();
        pl.setId(100L);
        pl.setGroup(g);
        proposal.setProductLine(pl);
        
        process = new Process();
        process.setId(10L);
        process.setProductLine(pl);
    }

    @Test
    void getType_ShouldReturn() {
        assertThat(handler.getType()).isEqualTo(ApprovalEntityType.TRAINING_SAMPLE_PROPOSAL);
    }

    @Test
    void findOverdueItems_ShouldReturn() {
        proposal.setUpdatedAt(LocalDateTime.now().minusDays(2));
        when(trainingSampleProposalRepository.findByStatusAndDeleteFlagFalse(ReportStatus.PENDING_APPROVAL))
                .thenReturn(List.of(proposal));

        List<OverdueItem> items = handler.findOverdueItems(ReportStatus.PENDING_APPROVAL, LocalDateTime.now().minusDays(1));
        
        assertThat(items).hasSize(1);
    }

    @Test
    void applyApproval_CreateProposal_ShouldCreateSample() {
        TrainingSampleProposalDetail detail = new TrainingSampleProposalDetail();
        detail.setId(100L);
        detail.setProposalType(ProposalType.CREATE);
        detail.setProcess(process);
        detail.setCategoryName("Cat1");
        detail.setTrainingDescription("Desc1");
        detail.setTrainingSampleCode("SC-001");
        proposal.getDetails().add(detail);

        when(trainingCodeGenerator.generateTrainingCode()).thenReturn("TR-001");
        when(trainingSampleRepository.findMaxProcessOrderByProcessId(10L)).thenReturn(0);
        when(trainingSampleRepository.findMaxCategoryOrderByProcessAndCategory(10L, "Cat1")).thenReturn(0);
        when(trainingSampleRepository.findMaxContentOrderByProcessCategoryAndDescription(10L, "Cat1", "Desc1")).thenReturn(0);
        
        when(trainingSampleRepository.save(any(TrainingSample.class))).thenAnswer(i -> {
            TrainingSample ts = i.getArgument(0);
            ts.setId(500L);
            return ts;
        });

        handler.applyApproval(proposal);

        verify(trainingSampleRepository).save(any(TrainingSample.class));
        verify(proposalDetailRepository).save(detail);
        assertThat(detail.getTrainingSample().getId()).isEqualTo(500L);
        assertThat(detail.getTrainingSample().getTrainingCode()).isEqualTo("TR-001");
    }

    @Test
    void applyApproval_DeleteProposal_ShouldSoftDelete() {
        TrainingSample existing = new TrainingSample();
        existing.setId(500L);
        
        TrainingSampleProposalDetail detail = new TrainingSampleProposalDetail();
        detail.setId(100L);
        detail.setProposalType(ProposalType.DELETE);
        detail.setTrainingSample(existing);
        
        proposal.getDetails().add(detail);

        when(trainingSampleRepository.findById(500L)).thenReturn(Optional.of(existing));

        handler.applyApproval(proposal);

        verify(trainingSampleRepository).save(existing);
        assertThat(existing.isDeleteFlag()).isTrue();
    }
}
