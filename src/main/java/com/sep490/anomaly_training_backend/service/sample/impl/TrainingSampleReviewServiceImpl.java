package com.sep490.anomaly_training_backend.service.sample.impl;

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
import com.sep490.anomaly_training_backend.exception.ErrorCode;
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
import com.sep490.anomaly_training_backend.service.sample.TrainingSampleReviewService;
import com.sep490.anomaly_training_backend.service.sample.TrainingSampleService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingSampleReviewServiceImpl implements TrainingSampleReviewService {
    private final TrainingSampleReviewPolicyRepository trainingSampleReviewPolicyRepository;
    private final ProductLineRepository productLineRepository;
    private final TrainingSampleReviewRepository trainingSampleReviewRepository;
    private final TrainingSampleReviewMapper trainingSampleReviewMapper;
    private final UserRepository userRepository;
    private final TrainingSampleReviewPolicyMapper trainingSampleReviewPolicyMapper;
    private final ApprovalService approvalService;
    private final TrainingSampleReviewScheduler scheduler;
    private final TrainingSampleService trainingSampleService;
    private final ObjectMapper objectMapper;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SUFFIX_LENGTH = 3; // Độ dài phần đuôi ngẫu nhiên
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public List<TrainingSampleReviewPolicyResponse> getTrainingSampleReviewPoliciesByProductLine(Long productLineId) {
        return trainingSampleReviewPolicyRepository.findByProductLineIdAndDeleteFlagFalseOrderByCreatedByAsc(productLineId).stream()
                .map(trainingSampleReviewPolicyMapper::toDto)
                .toList();
    }

    @Override
    public TrainingSampleReviewPolicyResponse createNewReviewPolicy(TrainingSampleReviewPolicyRequest request) {
        ProductLine productLine = productLineRepository.findById(request.getProductLineId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_LINE_NOT_FOUND));
        List<TrainingSampleReviewConfig> configList = new ArrayList<>();
        TrainingSampleReviewPolicy entity = new TrainingSampleReviewPolicy();
        entity.setProductLine(productLine);
        entity.setDescription(request.getDescription());
        entity.setPolicyName(request.getPolicyName());
        List<TrainingSampleReviewConfigRequest> reviewConfigRequests = request.getReviewConfigs();
        for (TrainingSampleReviewConfigRequest configRequest : reviewConfigRequests) {
            TrainingSampleReviewConfig config = new TrainingSampleReviewConfig();
            config.setTriggerDay(configRequest.getTriggerDay());
            config.setTriggerMonth(configRequest.getTriggerMonth());
            config.setDueDays(configRequest.getDueDays());
            config.setReviewPolicy(entity);
            configList.add(config);
        }
        entity.setReviewConfigs(configList);
        entity.setPolicyCode(generateReviewPolicyCode());
        List<TrainingSampleReviewPolicy> listPolicy = trainingSampleReviewPolicyRepository.findByProductLineIdAndStatusAndDeleteFlagFalseOrderByCreatedAtDesc(request.getProductLineId(), PolicyStatus.ACTIVE);
        for (TrainingSampleReviewPolicy policy : listPolicy) {
            if (policy.getStatus().equals(PolicyStatus.ACTIVE)) {
                policy.setExpirationDate(LocalDate.now());
            }
            policy.setStatus(PolicyStatus.ARCHIVED);
            trainingSampleReviewPolicyRepository.save(policy);
        }
        entity.setEffectiveDate(LocalDate.now());
        entity.setStatus(PolicyStatus.ACTIVE);
        TrainingSampleReviewPolicy savedPolicy = trainingSampleReviewPolicyRepository.save(entity);
        for (TrainingSampleReviewConfig config : savedPolicy.getReviewConfigs()) {
            scheduler.registerJob(config);
        }
        return trainingSampleReviewPolicyMapper.toDto(savedPolicy);
    }

    @Override
    public List<TrainingSampleReviewResponse> findByConfigId(Long configId) {
        return trainingSampleReviewRepository.findByConfigId(configId)
                .stream()
                .map(trainingSampleReviewMapper::toDto)
                .toList();
    }

    @Override
    public List<TrainingSampleReviewResponse> findByProductLine(Long productLineId) {
        return trainingSampleReviewRepository.findByProductLineIdOrderByCreatedAtDesc(productLineId)
                .stream()
                .map(trainingSampleReviewMapper::toDto)
                .toList();
    }

    @Override
    public void deletePolicy(Long policyId) {
        TrainingSampleReviewPolicy policy = trainingSampleReviewPolicyRepository.findById(policyId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_POLICY_NOT_FOUND));
        policy.setDeleteFlag(true);
        List<TrainingSampleReviewConfig> policyConfig = policy.getReviewConfigs();
        for (TrainingSampleReviewConfig config : policyConfig) {
            scheduler.removeJob(config.getId());
        }
        trainingSampleReviewPolicyRepository.save(policy);
    }

    @Override
    public TrainingSampleReviewResponse assignTeamLeadToReview(TrainingSampleReviewRequest request) {
        TrainingSampleReview review = trainingSampleReviewRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_REPORT_NOT_FOUND));
        User user = userRepository.findById(request.getTeamLeadId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        review.setReviewedBy(user);
        review.setStatus(ReportStatus.PENDING);
        return trainingSampleReviewMapper.toDto(trainingSampleReviewRepository.save(review));
    }

    @Override
    public TrainingSampleReviewResponse submit(TrainingSampleReviewRequest reviewRequest, User currentUser, HttpServletRequest request) {
        TrainingSampleReview review = trainingSampleReviewRepository.findById(reviewRequest.getId())
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_REPORT_NOT_FOUND));

        // Validate that review has a config and config has a policy
        if (review.getConfig() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST_FORMAT, "TrainingSampleReview config is null");
        }

        TrainingSampleReviewConfig config = review.getConfig();
        if (config.getReviewPolicy() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST_FORMAT, "TrainingSampleReviewConfig reviewPolicy is null");
        }

        TrainingSampleReviewPolicy policy = config.getReviewPolicy();
        if (policy.getProductLine() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST_FORMAT, "TrainingSampleReviewPolicy productLine is null");
        }

        Long productLineId = policy.getProductLine().getId();

        try {
            // Step 1: Fetch training sample data by product line
            List<TrainingSampleResponse> trainingSamples
                = trainingSampleService.getTrainingSampleByProductLine(productLineId);

            if (trainingSamples == null || trainingSamples.isEmpty()) {
                trainingSamples = new ArrayList<>();
            }

            // Step 2: Convert response to JSON string
            String sampleSnapshot = objectMapper.writeValueAsString(trainingSamples);

            // Step 3: Save JSON snapshot to sampleSnapshot field
            review.setSampleSnapshot(sampleSnapshot);
            review.setReviewDate(LocalDate.now());

            // Step 4: Save review to database
            review.setStatus(ReportStatus.PENDING);
            review = trainingSampleReviewRepository.save(review);

            // Step 5: Submit for approval
            approvalService.submit(review, currentUser, request);

            return trainingSampleReviewMapper.toDto(review);

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to serialize training sample snapshot: " + e.getMessage());
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Error during review submission: " + e.getMessage());
        }
    }

    @Override
    public void approve(Long id, User currentUser, ApproveRequest approveRequest, HttpServletRequest request) {
        TrainingSampleReview review = trainingSampleReviewRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_REPORT_NOT_FOUND));
        approvalService.approve(review, currentUser, approveRequest, request);
    }

    @Override
    public void revise(Long id, User currentUser, HttpServletRequest request) {
        TrainingSampleReview review = trainingSampleReviewRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_REPORT_NOT_FOUND));
        if (!currentUser.getUsername().equals(review.getCreatedBy())) {
            throw new AppException(ErrorCode.ONLY_AUTHOR_CAN_EDIT);
        }
        approvalService.revise(review, currentUser, request);
    }

    @Override
    public void reject(Long id, User currentUser, RejectRequest req, HttpServletRequest request) {
        TrainingSampleReview review = trainingSampleReviewRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_REPORT_NOT_FOUND));
        approvalService.reject(review, currentUser, req, request);
    }

    @Override
    public List<TrainingSampleReviewResponse> findByReviewedById(Long productLineId, Long reviewedId) {
        return trainingSampleReviewRepository.findReviewTask(productLineId, reviewedId)
                .stream()
                .map(trainingSampleReviewMapper::toDto)
                .toList();
    }

    private String generateReviewPolicyCode() {
        int currentYear = Year.now().getValue();
        StringBuilder suffix = new StringBuilder(SUFFIX_LENGTH);
        for (int i = 0; i < SUFFIX_LENGTH; i++) {
            int randomIndex = RANDOM.nextInt(CHARACTERS.length());
            suffix.append(CHARACTERS.charAt(randomIndex));
        }
        return String.format("RV%d%s", currentYear, suffix.toString());
    }

}
