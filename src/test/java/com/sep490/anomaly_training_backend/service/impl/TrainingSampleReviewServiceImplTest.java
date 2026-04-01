package com.sep490.anomaly_training_backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleReviewConfigRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleReviewPolicyRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleReviewRequest;
import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleResponse;
import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleReviewPolicyResponse;
import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleReviewResponse;
import com.sep490.anomaly_training_backend.enums.PolicyStatus;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleReviewMapper;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleReviewPolicyMapper;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.TrainingSampleReview;
import com.sep490.anomaly_training_backend.model.TrainingSampleReviewConfig;
import com.sep490.anomaly_training_backend.model.TrainingSampleReviewPolicy;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.ProductLineRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleReviewPolicyRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleReviewRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.scheduler.TrainingSampleReviewScheduler;
import com.sep490.anomaly_training_backend.service.TrainingSampleService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingSampleReviewServiceImplTest {

    @Mock private TrainingSampleReviewPolicyRepository trainingSampleReviewPolicyRepository;
    @Mock private ProductLineRepository productLineRepository;
    @Mock private TrainingSampleReviewRepository trainingSampleReviewRepository;
    @Mock private TrainingSampleReviewMapper trainingSampleReviewMapper;
    @Mock private UserRepository userRepository;
    @Mock private TrainingSampleReviewPolicyMapper trainingSampleReviewPolicyMapper;
    @Mock private ApprovalService approvalService;
    @Mock private TrainingSampleReviewScheduler scheduler;
    @Mock private TrainingSampleService trainingSampleService;
    @Mock private ObjectMapper objectMapper;

    @Mock private HttpServletRequest httpRequest;

    @InjectMocks
    private TrainingSampleReviewServiceImpl service;

    private User teamLead;
    private ProductLine productLine;
    private TrainingSampleReviewPolicy policy;
    private TrainingSampleReviewConfig config;
    private TrainingSampleReview review;

    @BeforeEach
    void setUp() {
        teamLead = new User();
        teamLead.setId(1L);
        teamLead.setUsername("teamlead");

        productLine = new ProductLine();
        productLine.setId(10L);

        policy = new TrainingSampleReviewPolicy();
        policy.setId(100L);
        policy.setProductLine(productLine);
        
        config = new TrainingSampleReviewConfig();
        config.setId(50L);
        config.setReviewPolicy(policy);
        policy.setReviewConfigs(List.of(config));

        review = new TrainingSampleReview();
        review.setId(200L);
        review.setConfig(config);
        review.setStatus(ReportStatus.REJECTED);
        review.setCurrentVersion(1);
    }

    @Test
    void getTrainingSampleReviewPoliciesByProductLine_Success() {
        when(trainingSampleReviewPolicyRepository.findByProductLineIdAndDeleteFlagFalseOrderByCreatedByAsc(10L))
                .thenReturn(List.of(policy));
        when(trainingSampleReviewPolicyMapper.toDto(policy)).thenReturn(mock(TrainingSampleReviewPolicyResponse.class));

        List<TrainingSampleReviewPolicyResponse> responses = service.getTrainingSampleReviewPoliciesByProductLine(10L);

        assertThat(responses).hasSize(1);
    }

    @Test
    void createNewReviewPolicy_Success() {
        TrainingSampleReviewPolicyRequest req = mock(TrainingSampleReviewPolicyRequest.class);
        when(req.getProductLineId()).thenReturn(10L);
        when(req.getPolicyName()).thenReturn("New Policy");
        
        TrainingSampleReviewConfigRequest confReq = mock(TrainingSampleReviewConfigRequest.class);
        when(confReq.getTriggerDay()).thenReturn(1);
        when(req.getReviewConfigs()).thenReturn(List.of(confReq));

        when(productLineRepository.findById(10L)).thenReturn(Optional.of(productLine));
        when(trainingSampleReviewPolicyRepository.findByProductLineIdAndStatusAndDeleteFlagFalseOrderByCreatedAtDesc(10L, PolicyStatus.ACTIVE))
                .thenReturn(new ArrayList<>());
        when(trainingSampleReviewPolicyRepository.save(any(TrainingSampleReviewPolicy.class))).thenReturn(policy);
        when(trainingSampleReviewPolicyMapper.toDto(policy)).thenReturn(mock(TrainingSampleReviewPolicyResponse.class));

        TrainingSampleReviewPolicyResponse res = service.createNewReviewPolicy(req);

        assertThat(res).isNotNull();
        verify(scheduler).registerJob(any(TrainingSampleReviewConfig.class));
    }

    @Test
    void deletePolicy_Success() {
        when(trainingSampleReviewPolicyRepository.findById(100L)).thenReturn(Optional.of(policy));

        service.deletePolicy(100L);

        assertThat(policy.isDeleteFlag()).isTrue();
        verify(scheduler).removeJob(50L);
        verify(trainingSampleReviewPolicyRepository).save(policy);
    }

    @Test
    void assignTeamLeadToReview_Success() {
        TrainingSampleReviewRequest req = mock(TrainingSampleReviewRequest.class);
        when(req.getId()).thenReturn(200L);
        when(req.getTeamLeadId()).thenReturn(1L);

        when(trainingSampleReviewRepository.findById(200L)).thenReturn(Optional.of(review));
        when(userRepository.findById(1L)).thenReturn(Optional.of(teamLead));
        when(trainingSampleReviewRepository.save(review)).thenReturn(review);
        when(trainingSampleReviewMapper.toDto(review)).thenReturn(mock(TrainingSampleReviewResponse.class));

        TrainingSampleReviewResponse res = service.assignTeamLeadToReview(req);

        assertThat(res).isNotNull();
        assertThat(review.getReviewedBy()).isEqualTo(teamLead);
        assertThat(review.getStatus()).isEqualTo(ReportStatus.ONGOING);
    }

    @Test
    void submit_Success() throws Exception {
        TrainingSampleReviewRequest req = mock(TrainingSampleReviewRequest.class);
        when(req.getId()).thenReturn(200L);

        when(trainingSampleReviewRepository.findById(200L)).thenReturn(Optional.of(review));
        when(trainingSampleService.getTrainingSampleByProductLine(10L)).thenReturn(List.of(mock(TrainingSampleResponse.class)));
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");
        when(trainingSampleReviewRepository.save(review)).thenReturn(review);
        when(trainingSampleReviewMapper.toDto(review)).thenReturn(mock(TrainingSampleReviewResponse.class));

        TrainingSampleReviewResponse res = service.submit(req, teamLead, httpRequest);

        assertThat(res).isNotNull();
        assertThat(review.getStatus()).isEqualTo(ReportStatus.PENDING_REVIEW);
        assertThat(review.getCurrentVersion()).isEqualTo(2);
        verify(approvalService).submit(review, teamLead, httpRequest);
    }

    @Test
    void approve_Success() {
        ApproveRequest req = new ApproveRequest();
        when(trainingSampleReviewRepository.findById(200L)).thenReturn(Optional.of(review));
        when(trainingSampleReviewRepository.save(review)).thenReturn(review);

        service.approve(200L, teamLead, req, httpRequest);

        assertThat(review.getConfirmedBy()).isEqualTo(teamLead);
        assertThat(review.getCompletedDate()).isEqualTo(LocalDate.now());
        verify(approvalService).approve(review, teamLead, req, httpRequest);
    }

    @Test
    void reject_Success() {
        RejectRequest req = new RejectRequest();
        when(trainingSampleReviewRepository.findById(200L)).thenReturn(Optional.of(review));

        service.reject(200L, teamLead, req, httpRequest);

        verify(approvalService).reject(review, teamLead, req, httpRequest);
    }
}
