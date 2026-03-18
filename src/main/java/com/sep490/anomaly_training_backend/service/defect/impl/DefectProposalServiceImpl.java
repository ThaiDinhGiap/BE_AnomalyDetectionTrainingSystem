package com.sep490.anomaly_training_backend.service.defect.impl;

import com.sep490.anomaly_training_backend.dto.request.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.request.DefectProposalDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.DefectProposalRequest;
import com.sep490.anomaly_training_backend.dto.request.RejectRequest;
import com.sep490.anomaly_training_backend.dto.response.DefectProposalDetailUpdateResponse;
import com.sep490.anomaly_training_backend.dto.response.DefectProposalResponse;
import com.sep490.anomaly_training_backend.dto.response.DefectProposalUpdateResponse;
import com.sep490.anomaly_training_backend.enums.DefectType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.DefectProposalDetailMapper;
import com.sep490.anomaly_training_backend.mapper.DefectProposalMapper;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.defect.DefectProposalService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DefectProposalServiceImpl implements DefectProposalService {
    private final DefectProposalRepository defectProposalRepository;
    private final DefectRepository defectRepository;
    private final DefectProposalMapper defectProposalMapper;
    private final DefectProposalDetailMapper defectProposalDetailMapper;
    private final UserRepository userRepository;
    private final ProductLineRepository productLineRepository;
    private final ProcessRepository processRepository;
    private final DefectProposalDetailRepository defectProposalDetailRepository;
    private final ApprovalService approvalService;
    private final AttachmentService attachmentService;
    private final ProductRepository productRepository;

    @Override
    public List<DefectProposalResponse> getDefectProposalByTeamLeadAndProductLine(Long id, String username) {
        List<DefectProposalResponse> result = new ArrayList<>();
        List<DefectProposal> listEntity = defectProposalRepository.findByProductLineIdAndCreatedBy(id, username);
        for (DefectProposal entity : listEntity) {
            result.add(defectProposalMapper.toResponse(entity, userRepository));
        }
        return result;
    }

    @Override
    @Transactional
    public void createDefectProposalDraft(DefectProposalRequest reportRequest, User currentUser) {
        ProductLine productLine = productLineRepository.findById(reportRequest.getProductLineId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_LINE_NOT_FOUND));
        DefectProposal proposalHeader = new DefectProposal();
        proposalHeader.setProductLine(productLine);
        proposalHeader.setStatus(ReportStatus.DRAFT);
        proposalHeader = defectProposalRepository.save(proposalHeader);
        createDetail(reportRequest.getListDetail(), proposalHeader, currentUser);
    }

    @Override
    public void deleteDefectProposal(Long id) {
        DefectProposal entity = defectProposalRepository.findById(id).orElse(null);
        if (entity != null) {
            entity.setDeleteFlag(true);
            defectProposalRepository.save(entity);
        }
    }

    @Override
    public DefectProposalUpdateResponse updateDefectProposal(Long id, DefectProposalRequest request, User currentUser) {
        DefectProposal proposal = defectProposalRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEFECT_PROPOSAL_NOT_FOUND));

        List<DefectProposalDetailRequest> items = request.getListDetail();
        if (items == null || items.isEmpty()) {
            throw new AppException(ErrorCode.PROPOSAL_HAS_NO_DETAILS);
        }
        // load existing details (of this proposal)
        List<DefectProposalDetail> existingDetails = defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(id);
        Map<Long, DefectProposalDetail> existingMap = new HashMap<>();
        for (DefectProposalDetail detail : existingDetails) {
            existingMap.put(detail.getId(), detail);
        }
        // validate ids belong to proposal
        for (DefectProposalDetailRequest item : items) {
            Long detailId = item.getDefectProposalDetailId();
            if (detailId != null && !existingMap.containsKey(detailId)) {
                throw new AppException(ErrorCode.INVALID_DETAIL_ID_FOR_PROPOSAL);
            }
        }
        // dùng để xác định những detail nào còn tồn tại trong request
        Set<Long> requestDetailIds = new HashSet<>();
        //apply create/update/delete
        for (DefectProposalDetailRequest item : items) {
            // create
            if (item.getDefectProposalDetailId() == null) {
                DefectProposalDetail newEntity = mapToEntity(item, proposal, currentUser);
                newEntity = defectProposalDetailRepository.save(newEntity);
                if (item.getImages() != null && !item.getImages().isEmpty()) {
                    attachmentService.uploadAttachments(item.getImages(), "DEFECT_PROPOSAL", newEntity.getId(), currentUser.getUsername());
                }
                continue;
            }
            requestDetailIds.add(item.getDefectProposalDetailId());
            DefectProposalDetail entity = existingMap.get(item.getDefectProposalDetailId());
            if (entity == null) {
                throw new AppException(ErrorCode.INVALID_DETAIL_ID_FOR_PROPOSAL);
            }
            if (item.getDefectId() != null) {
                Defect defectRef = defectRepository.getReferenceById(item.getDefectId());
                entity.setDefect(defectRef);
            } else {
                entity.setDefect(null);
            }
            
            // Validate and set product - can be null
            if (item.getProductId() != null) {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
                entity.setProduct(product);
            } else {
                entity.setProduct(null);
            }
            
            if (item.getProcessId() == null) {
                throw new AppException(ErrorCode.MISSING_PROCESS_ID);
            }
            Process processRef = processRepository.getReferenceById(item.getProcessId());
            entity.setProcess(processRef);
            entity.setProposalType(item.getProposalType());
            entity.setDefectDescription(item.getDefectDescription());
            entity.setDetectedDate(item.getDetectedDate());
            entity.setNote(item.getNote());
            entity.setOriginCause(item.getOriginCause());
            entity.setOutflowCause(item.getOutflowCause());
            entity.setCausePoint(item.getCausePoint());
            entity.setOriginMeasures(item.getOriginMeasures());
            entity.setOutflowMeasures(item.getOutflowMeasures());
            entity.setDefectType(DefectType.valueOf(item.getDefectType()));
            
            // Set new fields
            entity.setCustomer(item.getCustomer());
            entity.setQuantity(item.getQuantity());
            entity.setConclusion(item.getConclusion());
            if (item.getImages() != null && !item.getImages().isEmpty()) {
                attachmentService.uploadAttachments(item.getImages(), "DEFECT_PROPOSAL", entity.getId(), currentUser.getUsername());
                attachmentService.deleteAttachments("DEFECT_PROPOSAL", item.getDefectProposalDetailId());
            }
            defectProposalDetailRepository.save(entity);
        }
        for (DefectProposalDetail existing : existingDetails) {
            if (!requestDetailIds.contains(existing.getId())) {
                existing.setDeleteFlag(true);
                defectProposalDetailRepository.save(existing);
            }
        }
        DefectProposal updatedProposal = defectProposalRepository.save(proposal);
        List<DefectProposalDetail> latestDetails = defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(updatedProposal.getId());
        DefectProposalUpdateResponse response = new DefectProposalUpdateResponse();
        response.setId(updatedProposal.getId());
        response.setProductLineId(proposal.getProductLine() != null ? proposal.getProductLine().getId() : null);

        List<DefectProposalDetailUpdateResponse> detailResponses = new ArrayList<>();
        for (DefectProposalDetail detail : latestDetails) {
            detailResponses.add(mapToResponse(detail));
        }
        response.setDefectProposalDetail(detailResponses);
        return response;
    }

    @Override
    public void submitDefectProposalForApproval(Long proposalId, User currentUser, HttpServletRequest request) {
        DefectProposal proposal = defectProposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.DEFECT_PROPOSAL_NOT_FOUND));
        validateProposalForSubmission(proposal);
        approvalService.submit(proposal, currentUser, request);
        defectProposalRepository.save(proposal);
    }

    // Approval Methods
    @Override
    public void submit(Long proposalId, User currentUser, HttpServletRequest request) {
        DefectProposal proposal = defectProposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.DEFECT_PROPOSAL_NOT_FOUND));
        if (!proposal.getCreatedBy().equals(currentUser.getUsername())) {
            throw new AppException(ErrorCode.ONLY_AUTHOR_CAN_EDIT);
        }
        approvalService.submit(proposal, currentUser, request);
        defectProposalRepository.save(proposal);
    }

    @Override
    public void approve(Long proposalId, User currentUser, ApproveRequest req, HttpServletRequest request) {
        DefectProposal proposal = defectProposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.DEFECT_PROPOSAL_NOT_FOUND));
        approvalService.approve(proposal, currentUser, req, request);
        defectProposalRepository.save(proposal);
    }

    @Override
    public void reject(Long proposalId, User currentUser, RejectRequest req, HttpServletRequest request) {
        DefectProposal proposal = defectProposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.DEFECT_PROPOSAL_NOT_FOUND));
        approvalService.reject(proposal, currentUser, req, request);
        defectProposalRepository.save(proposal);
    }

    @Override
    public boolean canApprove(Long proposalId, User currentUser) {
        DefectProposal proposal = defectProposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.DEFECT_PROPOSAL_NOT_FOUND));
        return approvalService.canApprove(proposal, currentUser);
    }

    @Override
    public void revise(Long id, User currentUser, HttpServletRequest request) {
        DefectProposal proposal = defectProposalRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEFECT_PROPOSAL_NOT_FOUND));
        if (!currentUser.getUsername().equals(proposal.getCreatedBy())) {
            throw new AppException(ErrorCode.ONLY_AUTHOR_CAN_EDIT);
        }
        approvalService.revise(proposal, currentUser, request);
    }

    private void createDetail(List<DefectProposalDetailRequest> DefectProposalDetailList, DefectProposal proposal, User currentUser) {
        for (DefectProposalDetailRequest detailRequest : DefectProposalDetailList) {
            Process process = processRepository.findById(detailRequest.getProcessId())
                    .orElseThrow(() -> new AppException(ErrorCode.PROCESS_NOT_FOUND));
            DefectProposalDetail entity = new DefectProposalDetail();
            entity.setDefectProposal(proposal);
            
            // Handle defect - can be null
            if (detailRequest.getDefectId() != null) {
                Defect defect = defectRepository.findById(detailRequest.getDefectId()).orElse(null);
                entity.setDefect(defect);
            }
            
            // Handle product - can be null
            if (detailRequest.getProductId() != null) {
                Product product = productRepository.findById(detailRequest.getProductId())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
                entity.setProduct(product);
            }
            
            entity.setProposalType(detailRequest.getProposalType());
            entity.setDefectDescription(detailRequest.getDefectDescription());
            entity.setProcess(process);
            entity.setDetectedDate(detailRequest.getDetectedDate());
            entity.setNote(detailRequest.getNote());
            entity.setOriginCause(detailRequest.getOriginCause());
            entity.setOutflowCause(detailRequest.getOutflowCause());
            entity.setCausePoint(detailRequest.getCausePoint());
            entity.setOriginMeasures(detailRequest.getOriginMeasures());
            entity.setOutflowMeasures(detailRequest.getOutflowMeasures());
            entity.setDefectType(DefectType.valueOf(detailRequest.getDefectType()));
            
            // Set new fields
            entity.setCustomer(detailRequest.getCustomer());
            entity.setQuantity(detailRequest.getQuantity());
            entity.setConclusion(detailRequest.getConclusion());
            
            // Save detail first to get ID for attachment
            entity = defectProposalDetailRepository.save(entity);
            
            // Upload images for this detail if provided
            if (detailRequest.getImages() != null && !detailRequest.getImages().isEmpty()) {
                attachmentService.uploadAttachments(detailRequest.getImages(), "DEFECT_PROPOSAL", entity.getId(), currentUser.getUsername());
            }
        }
    }

    private DefectProposalDetail mapToEntity(DefectProposalDetailRequest request, DefectProposal proposal, User user) {
        if (request == null) throw new AppException(ErrorCode.INVALID_REQUEST_FORMAT);
        if (request.getProcessId() == null) throw new AppException(ErrorCode.MISSING_PROCESS_ID);
        if (request.getProposalType() == null) throw new AppException(ErrorCode.MISSING_PROPOSAL_TYPE);
        if (request.getDefectDescription() == null || request.getDefectDescription().isBlank()) {
            throw new AppException(ErrorCode.MISSING_DEFECT_DESCRIPTION);
        }
        if (request.getDetectedDate() == null) throw new AppException(ErrorCode.MISSING_DETECTED_DATE);

        DefectProposalDetail entity = new DefectProposalDetail();
        entity.setDefectProposal(proposal);

        if (request.getDefectId() != null) {
            Defect defect = defectRepository.getReferenceById(request.getDefectId());
            entity.setDefect(defect);
        } else {
            entity.setDefect(null);
        }

        // Validate and set product - can be null
        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            entity.setProduct(product);
        }

        Process process = processRepository.getReferenceById(request.getProcessId());
        entity.setProcess(process);

        entity.setProposalType(request.getProposalType());
        entity.setDefectDescription(request.getDefectDescription());
        entity.setDetectedDate(request.getDetectedDate());

        entity.setNote(request.getNote());
        entity.setOriginCause(request.getOriginCause());
        entity.setOutflowCause(request.getOutflowCause());
        entity.setCausePoint(request.getCausePoint());

        entity.setOriginMeasures(request.getOriginMeasures());
        entity.setOutflowMeasures(request.getOutflowMeasures());
        entity.setDefectType(DefectType.valueOf(request.getDefectType()));
        
        // Set new fields
        entity.setCustomer(request.getCustomer());
        entity.setQuantity(request.getQuantity());
        entity.setConclusion(request.getConclusion());


        entity.setDeleteFlag(false);

        return entity;
    }

    private DefectProposalDetailUpdateResponse mapToResponse(DefectProposalDetail entity) {
        if (entity == null) return null;
        DefectProposalDetailUpdateResponse response = new DefectProposalDetailUpdateResponse();

        response.setId(entity.getId());

        if (entity.getDefect() != null) {
            response.setDefectId(entity.getDefect().getId());
        }

        response.setProposalType(entity.getProposalType());
        response.setDefectDescription(entity.getDefectDescription());

        if (entity.getProcess() != null) {
            response.setProcessId(entity.getProcess().getId());
            response.setProcessName(entity.getProcess().getName());
        }
        response.setDetectedDate(entity.getDetectedDate());
        response.setNote(entity.getNote());
        response.setOriginCause(entity.getOriginCause());
        response.setOutflowCause(entity.getOutflowCause());
        response.setCausePoint(entity.getCausePoint());
        response.setDeleteFlag(entity.isDeleteFlag());
        response.setOriginMeasures(entity.getOriginMeasures());
        response.setOutflowMeasures(entity.getOutflowMeasures());
        response.setDefectType(entity.getDefectType().toString());
        return response;
    }

    private void validateProposalForSubmission(DefectProposal proposal) {
        if (proposal.getDetails() == null || proposal.getDetails().isEmpty()) {
            throw new AppException(ErrorCode.PROPOSAL_HAS_NO_DETAILS);
        }
        for (DefectProposalDetail detail : proposal.getDetails()) {
            if (detail.getProcess() == null) {
                throw new AppException(ErrorCode.MISSING_PROCESS_IN_DETAIL);
            }
            if (detail.getProposalType() == null) {
                throw new AppException(ErrorCode.MISSING_PROPOSAL_TYPE);
            }
            if (detail.getDefectDescription() == null || detail.getDefectDescription().isBlank()) {
                throw new AppException(ErrorCode.MISSING_DEFECT_DESCRIPTION);
            }
            if (detail.getDetectedDate() == null) {
                throw new AppException(ErrorCode.MISSING_DETECTED_DATE);
            }
        }
    }
}