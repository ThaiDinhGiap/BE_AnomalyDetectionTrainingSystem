package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.CreateTrainingSampleProposalDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.CreateTrainingSampleProposalRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleProposalDetailUpdateRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleProposalUpdateRequest;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalDetailUpdateResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalUpdateResponse;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.BusinessException;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleProposalMapper;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.TrainingSampleProposalService;

import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
        List<TrainingSampleProposal> entityList= trainingSampleProposalRepository.findByProductLineIdAndCreatedBy(id, username);
        List<TrainingSampleProposalResponse> trainingSampleProposalResponses = new ArrayList<>();
        for (TrainingSampleProposal entity : entityList) {
            trainingSampleProposalResponses.add(trainingSampleProposalMapper.toResponse(entity, userRepository));
        }
        return trainingSampleProposalResponses;
    }

    @Override
    public void createTrainingSampleProposal(CreateTrainingSampleProposalRequest proposalRequest) {
        ProductLine productLine = productLineRepository.findById(proposalRequest.getProductLineId()).get();
        TrainingSampleProposal proposal = new TrainingSampleProposal();
        proposal.setProductLine(productLine);
        proposal.setStatus(ReportStatus.DRAFT);
        TrainingSampleProposal newProposal = trainingSampleProposalRepository.save(proposal);
        createTrainingSampleProposalDetailRequest(proposalRequest.getTrainingSampleProposalDetail(), newProposal);
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
    public TrainingSampleProposalUpdateResponse updateTrainingSampleProposal(Long id, TrainingSampleProposalUpdateRequest request) throws BadRequestException {
        List<TrainingSampleProposalDetailUpdateRequest> items = request.getDetailUpdateRequests();
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

        // Validate detail id belongs to proposal
        for (TrainingSampleProposalDetailUpdateRequest item : items) {
            if (item.getId() != null && !existingMap.containsKey(item.getId())) {
                throw new BadRequestException(
                        "Detail id " + item.getId() + " does not belong to proposal " + id
                );
            }
        }
        // Create / Update / Delete
        for (TrainingSampleProposalDetailUpdateRequest item : items) {
            //  Create new
            if (item.getId() == null) {

                TrainingSampleProposalDetail newEntity = mapToEntity(item, proposal);
                trainingSampleProposalDetailRepository.save(newEntity);
                continue;
            }else{
                //Update or delete existing
                TrainingSampleProposalDetail entity = mapToEntity(item, proposal);
                entity.setId(item.getId());
                entity.setDeleteFlag(item.getDeleteFlag() != null && item.getDeleteFlag());
                trainingSampleProposalDetailRepository.save(entity);
            }

        }
        trainingSampleProposalRepository.save(proposal);

        //Build response
        List<TrainingSampleProposalDetail> latestDetails =
                trainingSampleProposalDetailRepository.findByTrainingSampleProposalIdAndDeleteFlagFalse(id);

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

    @Override
    public void revise(Long id, User currentUser, HttpServletRequest request) {
        TrainingSampleProposal proposal = trainingSampleProposalRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Training Sample Proposal not found"));

        if (!proposal.getCreatedBy().equals(proposal.getCreatedBy())) {
            throw new BusinessException("Only author can edit on this proposal");
        }
        approvalService.revise(proposal, currentUser, request);
    }


    private void createTrainingSampleProposalDetailRequest(List<CreateTrainingSampleProposalDetailRequest> proposalDetailList, TrainingSampleProposal Proposal) {
        for (CreateTrainingSampleProposalDetailRequest detailRequest : proposalDetailList) {
            Process process = processRepository.findById(detailRequest.getProcessId()).orElse(null);
            TrainingSampleProposalDetail entity = new TrainingSampleProposalDetail();
            entity.setTrainingSampleProposal(Proposal);
            if (detailRequest.getTrainingSampleId()!=null) {
                TrainingSample trainingSample = trainingSampleRepository.findById(detailRequest.getTrainingSampleId()).orElse(null);
                entity.setTrainingSample(trainingSample);
            }
            if (detailRequest.getDefectId()!=null) {
                Defect defect = defectRepository.findById(detailRequest.getDefectId()).orElse(null);
                entity.setDefect(defect);
            }
            entity.setProposalType(detailRequest.getProposalType());
            entity.setCategoryName(detailRequest.getCategoryName());
            entity.setProcess(process);
            entity.setTrainingDescription(detailRequest.getTrainingDescription());
            entity.setNote(detailRequest.getNote());
            trainingSampleProposalDetailRepository.save(entity);
        }
    }
    private TrainingSampleProposalDetail mapToEntity(
            TrainingSampleProposalDetailUpdateRequest request,
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
