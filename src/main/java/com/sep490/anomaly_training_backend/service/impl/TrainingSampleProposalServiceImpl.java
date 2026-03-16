package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleProposalDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleProposalRequest;
import com.sep490.anomaly_training_backend.dto.request.RejectRequest;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalDetailUpdateResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalUpdateResponse;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleProposalMapper;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.TrainingSampleProposalService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TrainingSampleProposalServiceImpl implements TrainingSampleProposalService {
    private final TrainingSampleProposalRepository trainingSampleProposalRepository;
    private final TrainingSampleProposalDetailRepository trainingSampleProposalDetailRepository;
    private final TrainingSampleRepository trainingSampleRepository;
    private final TrainingSampleProposalMapper trainingSampleProposalMapper;
    private final UserRepository userRepository;
    private final ProductLineRepository productLineRepository;
    private final DefectRepository defectRepository;
    private final ProcessRepository processRepository;
    private final ProductRepository productRepository;
    private final ApprovalService approvalService;
    private final AttachmentService attachmentService;

    @Override
    public List<TrainingSampleProposalResponse> getTrainingSampleProposalsByTeamLeadAndProductLine(Long id, String username) {
        List<TrainingSampleProposal> entityList = trainingSampleProposalRepository.findByProductLineIdAndCreatedBy(id, username);
        List<TrainingSampleProposalResponse> trainingSampleProposalResponses = new ArrayList<>();
        for (TrainingSampleProposal entity : entityList) {
            trainingSampleProposalResponses.add(trainingSampleProposalMapper.toResponse(entity, userRepository));
        }
        return trainingSampleProposalResponses;
    }

    @Override
    public void createTrainingSampleProposal(TrainingSampleProposalRequest proposalRequest, User currentUser) {
        ProductLine productLine = productLineRepository.findById(proposalRequest.getProductLineId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_LINE_NOT_FOUND));
        TrainingSampleProposal proposal = new TrainingSampleProposal();
        proposal.setProductLine(productLine);
        proposal.setStatus(ReportStatus.DRAFT);
        proposal = trainingSampleProposalRepository.save(proposal);
        createDetail(proposalRequest.getListDetail(), proposal, currentUser);
    }

    @Override
    public void deleteTrainingSampleProposal(Long id) {
        TrainingSampleProposal entity = trainingSampleProposalRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_PROPOSAL_NOT_FOUND));
        entity.setDeleteFlag(true);
        trainingSampleProposalRepository.save(entity);
    }

    @Override
    public TrainingSampleProposalUpdateResponse updateTrainingSampleProposal(Long id, TrainingSampleProposalRequest request) {
        List<TrainingSampleProposalDetailRequest> items = request.getListDetail();
        if (items == null || items.isEmpty()) {
            throw new AppException(ErrorCode.PROPOSAL_HAS_NO_DETAILS);
        }

        TrainingSampleProposal proposal = trainingSampleProposalRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_PROPOSAL_NOT_FOUND));

        if (request.getProductLineId() != null) {
            ProductLine productLine = productLineRepository.getReferenceById(request.getProductLineId());
            proposal.setProductLine(productLine);
        }

        List<TrainingSampleProposalDetail> existingDetails =
                trainingSampleProposalDetailRepository.findByTrainingSampleProposalIdAndDeleteFlagFalse(id);

        Map<Long, TrainingSampleProposalDetail> existingMap = new HashMap<>();
        for (TrainingSampleProposalDetail detail : existingDetails) {
            existingMap.put(detail.getId(), detail);
        }

        Set<Long> requestDetailIds = new HashSet<>();

        for (TrainingSampleProposalDetailRequest item : items) {
            if (item.getId() != null && !existingMap.containsKey(item.getId())) {
                throw new AppException(ErrorCode.INVALID_DETAIL_ID_FOR_PROPOSAL);
            }
        }
        for (TrainingSampleProposalDetailRequest item : items) {
            if (item.getId() == null) {
                TrainingSampleProposalDetail newEntity = mapToEntity(item, proposal);
                trainingSampleProposalDetailRepository.save(newEntity);
                continue;
            }
            requestDetailIds.add(item.getId());
            TrainingSampleProposalDetail entity = mapToEntity(item, proposal);
            entity.setId(item.getId());
            trainingSampleProposalDetailRepository.save(entity);
        }
        for (TrainingSampleProposalDetail existing : existingDetails) {
            if (!requestDetailIds.contains(existing.getId())) {
                existing.setDeleteFlag(true);
                trainingSampleProposalDetailRepository.save(existing);
            }
        }
        trainingSampleProposalRepository.save(proposal);

        List<TrainingSampleProposalDetail> latestDetails = trainingSampleProposalDetailRepository.findByTrainingSampleProposalIdAndDeleteFlagFalse(id);
        List<TrainingSampleProposalDetailUpdateResponse> detailResponses = new ArrayList<>();
        for (TrainingSampleProposalDetail detail : latestDetails) {
            detailResponses.add(mapToResponse(detail));
        }

        return TrainingSampleProposalUpdateResponse.builder()
                .id(proposal.getId())
                .productLineId(proposal.getProductLine() != null ? proposal.getProductLine().getId() : null)
                .detailUpdateResponses(detailResponses)
                .build();
    }

    @Override
    @Transactional
    public void revise(Long id, User currentUser, HttpServletRequest request) {
        TrainingSampleProposal proposal = trainingSampleProposalRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_PROPOSAL_NOT_FOUND));
        if (!proposal.getCreatedBy().equals(proposal.getCreatedBy())) {
            throw new AppException(ErrorCode.ONLY_AUTHOR_CAN_EDIT);
        }
        approvalService.revise(proposal, currentUser, request);
        trainingSampleProposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public void submit(Long proposalId, User currentUser, HttpServletRequest request) {
        TrainingSampleProposal proposal = trainingSampleProposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_PROPOSAL_NOT_FOUND));
        approvalService.submit(proposal, currentUser, request);
        trainingSampleProposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public void approve(Long proposalId, User currentUser, ApproveRequest req, HttpServletRequest request) {
        TrainingSampleProposal proposal = trainingSampleProposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_PROPOSAL_NOT_FOUND));
        approvalService.approve(proposal, currentUser, req, request);
        trainingSampleProposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public void reject(Long proposalId, User currentUser, RejectRequest req, HttpServletRequest request) {
        TrainingSampleProposal proposal = trainingSampleProposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_PROPOSAL_NOT_FOUND));
        approvalService.reject(proposal, currentUser, req, request);
        trainingSampleProposalRepository.save(proposal);
    }

    @Override
    public boolean canApprove(Long proposalId, User currentUser) {
        TrainingSampleProposal proposal = trainingSampleProposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_PROPOSAL_NOT_FOUND));
        return approvalService.canApprove(proposal, currentUser);
    }

    @Override
    public void submitTrainingSampleProposalForApproval(Long proposalId, User currentUser, HttpServletRequest request) {
        TrainingSampleProposal proposal = trainingSampleProposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_PROPOSAL_NOT_FOUND));
        validateProposalForSubmission(proposal);
        approvalService.submit(proposal, currentUser, request);
        trainingSampleProposalRepository.save(proposal);
    }

    private void createDetail(List<TrainingSampleProposalDetailRequest> proposalDetailList, TrainingSampleProposal proposal, User currentUser) {
        for (TrainingSampleProposalDetailRequest detailRequest : proposalDetailList) {
            Process process = processRepository.findById(detailRequest.getProcessId())
                    .orElseThrow(() -> new AppException(ErrorCode.PROCESS_NOT_FOUND));
            TrainingSampleProposalDetail entity = new TrainingSampleProposalDetail();
            
            // Handle trainingSample - can be null
            if (detailRequest.getTrainingSampleId() != null) {
                TrainingSample trainingSample = trainingSampleRepository.findById(detailRequest.getTrainingSampleId()).orElse(null);
                entity.setTrainingSample(trainingSample);
            }
            
            // Handle defect - validate if not null
            if (detailRequest.getDefectId() != null) {
                Defect defect = defectRepository.findById(detailRequest.getDefectId())
                        .orElseThrow(() -> new AppException(ErrorCode.DEFECT_NOT_FOUND));
                entity.setDefect(defect);
            }
            
            // Handle product - validate if not null
            if (detailRequest.getProductId() != null) {
                Product product = productRepository.findById(detailRequest.getProductId())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
                entity.setProduct(product);
            }
            
            entity.setTrainingSampleProposal(proposal);
            entity.setProposalType(detailRequest.getProposalType());
            entity.setCategoryName(detailRequest.getCategoryName());
            entity.setProcess(process);
            entity.setTrainingDescription(detailRequest.getTrainingDescription());
            entity.setTrainingSampleCode(detailRequest.getTrainingSampleCode());
            entity.setNote(detailRequest.getNote());
            
            // Save detail first to get ID for attachment
            entity = trainingSampleProposalDetailRepository.save(entity);
            
            // Upload images for this detail if provided
            if (detailRequest.getImages() != null && !detailRequest.getImages().isEmpty()) {
                attachmentService.uploadAttachments(detailRequest.getImages(), "TRAINING_SAMPLE_PROPOSAL", entity.getId(), currentUser.getUsername());
            }
        }
    }

    private TrainingSampleProposalDetail mapToEntity(TrainingSampleProposalDetailRequest request, TrainingSampleProposal proposal) {
        if (request == null) throw new AppException(ErrorCode.INVALID_REQUEST_FORMAT);
        if (request.getProposalType() == null) throw new AppException(ErrorCode.MISSING_PROPOSAL_TYPE);
        if (request.getProcessId() == null) throw new AppException(ErrorCode.MISSING_PROCESS_ID);
        if (request.getCategoryName() == null || request.getCategoryName().isBlank()) {
            throw new AppException(ErrorCode.MISSING_CATEGORY_NAME);
        }
        if (request.getTrainingDescription() == null || request.getTrainingDescription().isBlank()) {
            throw new AppException(ErrorCode.MISSING_TRAINING_DESCRIPTION);
        }

        TrainingSampleProposalDetail entity = new TrainingSampleProposalDetail();
        entity.setTrainingSampleProposal(proposal);
        entity.setProposalType(request.getProposalType());
        entity.setProcess(processRepository.getReferenceById(request.getProcessId()));

        if (request.getTrainingSampleId() != null) {
            entity.setTrainingSample(trainingSampleRepository.getReferenceById(request.getTrainingSampleId()));
        } else {
            entity.setTrainingSample(null);
        }

        if (request.getProductId() != null) {
            entity.setProduct(productRepository.getReferenceById(request.getProductId()));
        } else {
            entity.setProduct(null);
        }

        if (request.getDefectId() != null) {
            entity.setDefect(defectRepository.getReferenceById(request.getDefectId()));
        } else {
            entity.setDefect(null);
        }
        entity.setCategoryName(request.getCategoryName());
        entity.setTrainingSampleCode(request.getTrainingSampleCode());
        entity.setTrainingDescription(request.getTrainingDescription());
        entity.setNote(request.getNote());
        entity.setDeleteFlag(false);
        return entity;
    }

    private TrainingSampleProposalDetailUpdateResponse mapToResponse(TrainingSampleProposalDetail entity) {
        if (entity == null) return null;
        Long trainingSampleId = entity.getTrainingSample() != null ? entity.getTrainingSample().getId() : null;
        Long processId = entity.getProcess() != null ? entity.getProcess().getId() : null;
        Long productId = entity.getProduct() != null ? entity.getProduct().getId() : null;
        Long defectId = entity.getDefect() != null ? entity.getDefect().getId() : null;

        return TrainingSampleProposalDetailUpdateResponse.builder()
                .id(entity.getId())
                .trainingSampleId(trainingSampleId)
                .proposalType(entity.getProposalType())
                .processId(processId)
                .productId(productId)
                .defectId(defectId)
                .categoryName(entity.getCategoryName())
                .trainingSampleCode(entity.getTrainingSampleCode())
                .trainingDescription(entity.getTrainingDescription())
                .note(entity.getNote())
                .build();
    }

    private void validateProposalForSubmission(TrainingSampleProposal proposal) {
        if (proposal.getDetails() == null || proposal.getDetails().isEmpty()) {
            throw new AppException(ErrorCode.PROPOSAL_HAS_NO_DETAILS);
        }
        for (TrainingSampleProposalDetail detail : proposal.getDetails()) {
            if (detail.getProposalType() == null) {
                throw new AppException(ErrorCode.MISSING_PROPOSAL_TYPE);
            }
            if (detail.getProcess() == null) {
                throw new AppException(ErrorCode.MISSING_PROCESS_IN_DETAIL);
            }
            if (detail.getCategoryName() == null || detail.getCategoryName().isBlank()) {
                throw new AppException(ErrorCode.MISSING_CATEGORY_NAME);
            }
            if (detail.getTrainingDescription() == null || detail.getTrainingDescription().isBlank()) {
                throw new AppException(ErrorCode.MISSING_TRAINING_DESCRIPTION);
            }
        }
    }
}