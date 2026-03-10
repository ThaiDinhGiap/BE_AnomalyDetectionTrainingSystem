package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleProposalDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleProposalRequest;
import com.sep490.anomaly_training_backend.dto.request.RejectRequest;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalDetailUpdateResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalUpdateResponse;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.BusinessException;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleProposalMapper;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.DefectRepository;
import com.sep490.anomaly_training_backend.repository.ProcessRepository;
import com.sep490.anomaly_training_backend.repository.ProductLineRepository;
import com.sep490.anomaly_training_backend.repository.ProductRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleProposalDetailRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleProposalRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.TrainingSampleProposalService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void createTrainingSampleProposal(TrainingSampleProposalRequest proposalRequest) {
        ProductLine productLine = productLineRepository.findById(proposalRequest.getProductLineId()).get();
        TrainingSampleProposal proposal = new TrainingSampleProposal();
        proposal.setProductLine(productLine);
        proposal.setStatus(ReportStatus.DRAFT);
        proposal.setDetails(createDetail(proposalRequest.getListDetail(), proposal));
        trainingSampleProposalRepository.save(proposal);
    }

    @Override
    public void deleteTrainingSampleProposal(Long id) {
        TrainingSampleProposal entity = trainingSampleProposalRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Training Sample Proposal not found"));
        if (entity != null) {
            entity.setDeleteFlag(true);
            trainingSampleProposalRepository.save(entity);
        }

    }

    @Override
    public TrainingSampleProposalUpdateResponse updateTrainingSampleProposal(Long id, TrainingSampleProposalRequest request) throws BadRequestException {
        List<TrainingSampleProposalDetailRequest> items = request.getListDetail();
        if (items == null || items.isEmpty()) {
            throw new BadRequestException("Training sample proposal must contain at least 1 detail");
        }

        // Load proposal
        TrainingSampleProposal proposal = trainingSampleProposalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Training sample proposal not found"));

        //Update productLine nếu có
        if (request.getProductLineId() != null) {
            ProductLine productLine = productLineRepository.getReferenceById(request.getProductLineId());
            proposal.setProductLine(productLine);
        }

        //Load existing details of this proposal
        List<TrainingSampleProposalDetail> existingDetails =
                trainingSampleProposalDetailRepository.findByTrainingSampleProposalIdAndDeleteFlagFalse(id);

        Map<Long, TrainingSampleProposalDetail> existingMap = new HashMap<>();
        for (TrainingSampleProposalDetail detail : existingDetails) {
            existingMap.put(detail.getId(), detail);
        }

        // Ghi nhận những detail cũ nào vẫn còn xuất hiện trong request
        Set<Long> requestDetailIds = new HashSet<>();

        // Validate detail id belongs to proposal
        for (TrainingSampleProposalDetailRequest item : items) {
            if (item.getId() != null && !existingMap.containsKey(item.getId())) {
                throw new BadRequestException(
                        "Detail id " + item.getId() + " does not belong to proposal " + id
                );
            }
        }
        // Create / Update / Delete
        for (TrainingSampleProposalDetailRequest item : items) {
            //  Create new
            if (item.getId() == null) {
                TrainingSampleProposalDetail newEntity = mapToEntity(item, proposal);
                trainingSampleProposalDetailRepository.save(newEntity);
                continue;
            }
            // Update existing
            requestDetailIds.add(item.getId());
            //Update
            TrainingSampleProposalDetail entity = mapToEntity(item, proposal);
            entity.setId(item.getId());
            trainingSampleProposalDetailRepository.save(entity);


        }
        // Delete những detail cũ có trong DB nhưng request không gửi lên
        for (TrainingSampleProposalDetail existing : existingDetails) {
            if (!requestDetailIds.contains(existing.getId())) {
                existing.setDeleteFlag(true);
                trainingSampleProposalDetailRepository.save(existing);
            }
        }
        trainingSampleProposalRepository.save(proposal);

        //Build response
        List<TrainingSampleProposalDetail> latestDetails = trainingSampleProposalDetailRepository.findByTrainingSampleProposalIdAndDeleteFlagFalse(id);

        List<TrainingSampleProposalDetailUpdateResponse> detailResponses = new ArrayList<>();
        for (TrainingSampleProposalDetail detail : latestDetails) {
            detailResponses.add(mapToResponse(detail));
        }

        return TrainingSampleProposalUpdateResponse.builder()
                .id(proposal.getId())
                .productLineId(
                        proposal.getProductLine() != null
                                ? proposal.getProductLine().getId()
                                : null
                )
                .detailUpdateResponses(detailResponses)
                .build();
    }

    // Approval Methods
    @Override
    @Transactional
    public void revise(Long id, User currentUser, HttpServletRequest request) {
        TrainingSampleProposal proposal = trainingSampleProposalRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Training Sample Proposal not found"));

        if (!proposal.getCreatedBy().equals(proposal.getCreatedBy())) {
            throw new BusinessException("Only author can edit on this proposal");
        }

        approvalService.revise(proposal, currentUser, request);
        trainingSampleProposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public void submit(Long proposalId, User currentUser, HttpServletRequest request) {
        TrainingSampleProposal proposal = trainingSampleProposalRepository.findById(proposalId).orElseThrow(() -> new EntityNotFoundException("Training Sample Proposal not found"));
        approvalService.submit(proposal, currentUser, request);
        trainingSampleProposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public void approve(Long proposalId, User currentUser, ApproveRequest req, HttpServletRequest request) {
        TrainingSampleProposal proposal = trainingSampleProposalRepository.findById(proposalId).orElseThrow(() -> new EntityNotFoundException("Training Sample Proposal not found"));
        approvalService.approve(proposal, currentUser, req, request);
        trainingSampleProposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public void reject(Long proposalId, User currentUser, RejectRequest req, HttpServletRequest request) {
        TrainingSampleProposal proposal = trainingSampleProposalRepository.findById(proposalId).orElseThrow(() -> new EntityNotFoundException("Training Sample Proposal not found"));
        approvalService.reject(proposal, currentUser, req, request);
        trainingSampleProposalRepository.save(proposal);
    }

    @Override
    public boolean canApprove(Long proposalId, User currentUser) {
        TrainingSampleProposal proposal = trainingSampleProposalRepository.findById(proposalId).orElseThrow(() -> new EntityNotFoundException("Training Sample Proposal not found"));
        return approvalService.canApprove(proposal, currentUser);
    }

    private List<TrainingSampleProposalDetail> createDetail(List<TrainingSampleProposalDetailRequest> proposalDetailList, TrainingSampleProposal proposal) {
        List<TrainingSampleProposalDetail> detailList = new ArrayList<>();
        for (TrainingSampleProposalDetailRequest detailRequest : proposalDetailList) {
            Process process = processRepository.findById(detailRequest.getProcessId()).orElse(null);
            TrainingSampleProposalDetail entity = new TrainingSampleProposalDetail();
            if (detailRequest.getTrainingSampleId() != null) {
                TrainingSample trainingSample = trainingSampleRepository.findById(detailRequest.getTrainingSampleId()).orElse(null);
                entity.setTrainingSample(trainingSample);
            }
            if (detailRequest.getDefectId() != null) {
                Defect defect = defectRepository.findById(detailRequest.getDefectId()).orElse(null);
                entity.setDefect(defect);
            }
            if (detailRequest.getProductId() != null) {
                Product product = productRepository.findById(detailRequest.getProductId()).orElse(null);
                entity.setProduct(product);
            }
            entity.setTrainingSampleProposal(proposal);
            entity.setProposalType(detailRequest.getProposalType());
            entity.setCategoryName(detailRequest.getCategoryName());
            entity.setProcess(process);
            entity.setTrainingDescription(detailRequest.getTrainingDescription());
            entity.setTrainingSampleCode(detailRequest.getTrainingSampleCode());
            entity.setNote(detailRequest.getNote());
            detailList.add(entity);
        }
        return detailList;
    }

    private TrainingSampleProposalDetail mapToEntity(
            TrainingSampleProposalDetailRequest request,
            TrainingSampleProposal proposal
    ) {
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null");
        }

        // required fields (theo entity nullable=false)
        if (request.getProposalType() == null) {
            throw new IllegalArgumentException("proposalType is required");
        }
        if (request.getProcessId() == null) {
            throw new IllegalArgumentException("processId is required");
        }
        if (request.getCategoryName() == null || request.getCategoryName().isBlank()) {
            throw new IllegalArgumentException("categoryName is required");
        }
        if (request.getTrainingDescription() == null || request.getTrainingDescription().isBlank()) {
            throw new IllegalArgumentException("trainingDescription is required");
        }

        TrainingSampleProposalDetail entity = new TrainingSampleProposalDetail();
        // FK bắt buộc
        entity.setTrainingSampleProposal(proposal);
        entity.setProposalType(request.getProposalType());
        entity.setProcess(processRepository.getReferenceById(request.getProcessId()));

        // FK optional
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
        // fields thường
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
}
