package com.sep490.anomaly_training_backend.service.approval.handler;

import com.sep490.anomaly_training_backend.dto.approval.OverdueItem;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.DefectType;
import com.sep490.anomaly_training_backend.enums.ProposalType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.model.Attachment;
import com.sep490.anomaly_training_backend.model.Defect;
import com.sep490.anomaly_training_backend.model.DefectProposal;
import com.sep490.anomaly_training_backend.model.DefectProposalDetail;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.repository.AttachmentRepository;
import com.sep490.anomaly_training_backend.repository.DefectProposalDetailRepository;
import com.sep490.anomaly_training_backend.repository.DefectProposalRepository;
import com.sep490.anomaly_training_backend.repository.DefectRepository;
import com.sep490.anomaly_training_backend.repository.ProcessRepository;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import com.sep490.anomaly_training_backend.util.DefectCodeGenerator;
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
class DefectProposalApprovalHandlerTest {

    @Mock
    private DefectProposalRepository defectProposalRepository;
    @Mock
    private DefectProposalDetailRepository defectProposalDetailRepository;
    @Mock
    private DefectRepository defectRepository;
    @Mock
    private DefectCodeGenerator defectCodeGenerator;
    @Mock
    private ProcessRepository processRepository;
    @Mock
    private AttachmentService attachmentService;
    @Mock
    private AttachmentRepository attachmentRepository;

    @InjectMocks
    private DefectProposalApprovalHandler handler;

    private DefectProposal proposal;
    private Process process;

    @BeforeEach
    void setUp() {
        proposal = new DefectProposal();
        proposal.setId(1L);
        Group g = new Group();
        g.setId(100L);
        ProductLine pl = new ProductLine();
        pl.setGroup(g);
        proposal.setProductLine(pl);
        
        process = new Process();
        process.setId(10L);
        process.setClassification(com.sep490.anomaly_training_backend.enums.ProcessClassification.C2);
    }

    @Test
    void getType_ShouldReturnDefectProposal() {
        assertThat(handler.getType()).isEqualTo(ApprovalEntityType.DEFECT_PROPOSAL);
    }

    @Test
    void getDisplayLabel_ShouldReturnCorrectLabel() {
        assertThat(handler.getDisplayLabel()).isEqualTo("đề xuất lỗi");
    }

    @Test
    void findOverdueItems_ShouldReturnOverdue() {
        proposal.setUpdatedAt(LocalDateTime.now().minusDays(2));
        when(defectProposalRepository.findByStatusAndDeleteFlagFalse(ReportStatus.PENDING_REVIEW))
                .thenReturn(List.of(proposal));

        List<OverdueItem> items = handler.findOverdueItems(ReportStatus.PENDING_REVIEW, LocalDateTime.now().minusDays(1));
        
        assertThat(items).hasSize(1);
        assertThat(items.get(0).entityId()).isEqualTo(1L);
        // group id will be null since we can't set it easily, which is fine
    }

    @Test
    void applyApproval_WhenNoDetails_ShouldThrowException() {
        assertThatThrownBy(() -> handler.applyApproval(proposal))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no details");
    }

    @Test
    void applyApproval_CreateProposal_ShouldCreateDefect() {
        DefectProposalDetail detail = new DefectProposalDetail();
        detail.setId(100L);
        detail.setProposalType(ProposalType.CREATE);
        detail.setDefectDescription("Test Desc");
        detail.setProcess(process);
        detail.setDefectType(DefectType.DEFECTIVE_GOODS);
        detail.setDetectedDate(java.time.LocalDate.now());
        
        proposal.getDetails().add(detail);

        when(defectCodeGenerator.generateDefectCode()).thenReturn("DEFECT-001");
        when(defectRepository.save(any(Defect.class))).thenAnswer(i -> {
            Defect d = i.getArgument(0);
            d.setId(500L);
            return d;
        });

        handler.applyApproval((com.sep490.anomaly_training_backend.model.Approvable) proposal);

        verify(processRepository).save(process);
        verify(defectRepository).save(any(Defect.class));
        assertThat(detail.getDefect().getId()).isEqualTo(500L);
        assertThat(process.getClassification()).isEqualTo(com.sep490.anomaly_training_backend.enums.ProcessClassification.C1);
    }

    @Test
    void applyApproval_UpdateProposal_ShouldUpdateDefect() {
        Defect existingDefect = new Defect();
        existingDefect.setId(500L);
        existingDefect.setDefectCode("DEFECT-001");
        
        DefectProposalDetail detail = new DefectProposalDetail();
        detail.setId(100L);
        detail.setProposalType(ProposalType.UPDATE);
        detail.setDefect(existingDefect);
        detail.setDefectDescription("Updated Desc");
        detail.setProcess(process);
        detail.setDefectType(DefectType.DEFECTIVE_GOODS);
        detail.setDetectedDate(java.time.LocalDate.now());
        
        proposal.getDetails().add(detail);

        when(defectRepository.findById(500L)).thenReturn(Optional.of(existingDefect));
        when(defectRepository.save(any(Defect.class))).thenReturn(existingDefect);
        
        Attachment att = new Attachment();
        when(attachmentService.getAttachmentsByEntity("TRAINING_SAMPLE_PROPOSAL", 100L))
                .thenReturn(List.of(att));

        handler.applyApproval((com.sep490.anomaly_training_backend.model.Approvable) proposal);

        verify(defectRepository).save(existingDefect);
        verify(attachmentService).deleteAttachments("TRAINING_SAMPLE", 500L);
        verify(attachmentRepository).save(any(Attachment.class));
        assertThat(existingDefect.getDefectDescription()).isEqualTo("Updated Desc");
    }

    @Test
    void applyApproval_DeleteProposal_ShouldHardDeleteDefect() {
        Defect existingDefect = new Defect();
        existingDefect.setId(500L);
        
        DefectProposalDetail detail = new DefectProposalDetail();
        detail.setId(100L);
        detail.setProposalType(ProposalType.DELETE);
        detail.setDefect(existingDefect);
        detail.setProcess(process);
        detail.setDefectType(DefectType.DEFECTIVE_GOODS);
        
        proposal.getDetails().add(detail);

        when(defectRepository.findById(500L)).thenReturn(Optional.of(existingDefect));

        handler.applyApproval((com.sep490.anomaly_training_backend.model.Approvable) proposal);

        verify(defectRepository).delete(existingDefect);
    }
}
