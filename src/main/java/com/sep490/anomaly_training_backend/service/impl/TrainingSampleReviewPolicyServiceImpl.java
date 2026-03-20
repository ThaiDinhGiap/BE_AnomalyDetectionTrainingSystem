package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleReviewConfigRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleReviewPolicyRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleReviewRequest;
import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleReviewPolicyResponse;
import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleReviewResponse;
import com.sep490.anomaly_training_backend.enums.PolicyStatus;
import com.sep490.anomaly_training_backend.enums.TrainingSampleReviewResult;
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
import com.sep490.anomaly_training_backend.service.TrainingSampleReviewPolicyService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
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
public class TrainingSampleReviewPolicyServiceImpl implements TrainingSampleReviewPolicyService {
    private final TrainingSampleReviewPolicyRepository trainingSampleReviewPolicyRepository;
    private final ProductLineRepository productLineRepository;
    private final TrainingSampleReviewRepository trainingSampleReviewRepository;
    private final TrainingSampleReviewMapper trainingSampleReviewMapper;
    private final UserRepository userRepository;
    private final TrainingSampleReviewPolicyMapper trainingSampleReviewPolicyMapper;
    private final ApprovalService approvalService;
    private final TrainingSampleReviewScheduler scheduler;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SUFFIX_LENGTH = 3; // Độ dài phần đuôi ngẫu nhiên
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public List<TrainingSampleReviewPolicyResponse> getTrainingSampleReviewPoliciesByProductLine(Long productLineId) {
        return trainingSampleReviewPolicyRepository.findByProductLineIdAndDeleteFlagFalse(productLineId).stream()
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
        List<TrainingSampleReviewPolicy> listPolicy = trainingSampleReviewPolicyRepository.findByProductLineIdAndStatusAndDeleteFlagFalse(request.getProductLineId(), PolicyStatus.ACTIVE);
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
        return trainingSampleReviewRepository.findByProductLineId(productLineId)
                .stream()
                .map(trainingSampleReviewMapper::toDto)
                .toList();
    }

    @Override
    public void deletePolicy(Long policyId) {
        TrainingSampleReviewPolicy policy = trainingSampleReviewPolicyRepository.findById(policyId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_POLICY_NOT_FOUND));
        policy.setDeleteFlag(true);
        trainingSampleReviewPolicyRepository.save(policy);
    }

    @Override
    public TrainingSampleReviewResponse assignTeamLeadToReview(TrainingSampleReviewRequest request) {
        TrainingSampleReview review = trainingSampleReviewRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_REPORT_NOT_FOUND));
        User user = userRepository.findById(request.getTeamLeadId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        review.setReviewedBy(user);
        review.setResult(TrainingSampleReviewResult.PENDING);
        return trainingSampleReviewMapper.toDto(trainingSampleReviewRepository.save(review));
    }

    @Override
    public TrainingSampleReviewResponse confirmReviewByTeamLead(TrainingSampleReviewRequest request) {
        TrainingSampleReview review = trainingSampleReviewRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_REPORT_NOT_FOUND));

        review.setSampleSnapshot(request.getSampleSnapshot());
        review.setResult(TrainingSampleReviewResult.NEED_VERIFIED);
        trainingSampleReviewRepository.save(review);
        return trainingSampleReviewMapper.toDto(trainingSampleReviewRepository.save(review));
    }

    @Override
    public void approve(Long id, User currentUser, ApproveRequest approveRequest, HttpServletRequest request) {
        TrainingSampleReview review = trainingSampleReviewRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_REPORT_NOT_FOUND));
        approvalService.canApprove(review, currentUser);
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
