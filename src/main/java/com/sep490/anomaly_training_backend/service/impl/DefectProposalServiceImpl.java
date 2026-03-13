package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.request.DefectProposalDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.DefectProposalRequest;
import com.sep490.anomaly_training_backend.dto.request.RejectRequest;
import com.sep490.anomaly_training_backend.dto.response.DefectProposalDetailUpdateResponse;
import com.sep490.anomaly_training_backend.dto.response.DefectProposalResponse;
import com.sep490.anomaly_training_backend.dto.response.DefectProposalUpdateResponse;
import com.sep490.anomaly_training_backend.enums.DefectType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.BusinessException;
import com.sep490.anomaly_training_backend.exception.ResourceNotFoundException;
import com.sep490.anomaly_training_backend.mapper.DefectProposalDetailMapper;
import com.sep490.anomaly_training_backend.mapper.DefectProposalMapper;
import com.sep490.anomaly_training_backend.model.Defect;
import com.sep490.anomaly_training_backend.model.DefectProposal;
import com.sep490.anomaly_training_backend.model.DefectProposalDetail;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.DefectProposalDetailRepository;
import com.sep490.anomaly_training_backend.repository.DefectProposalRepository;
import com.sep490.anomaly_training_backend.repository.DefectRepository;
import com.sep490.anomaly_training_backend.repository.ProcessRepository;
import com.sep490.anomaly_training_backend.repository.ProductLineRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.DefectProposalService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

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
    public void createDefectProposalDraft(DefectProposalRequest reportRequest) {
        ProductLine productLine = productLineRepository.findById(reportRequest.getProductLineId()).get();
        DefectProposal proposalHeader = new DefectProposal();
        proposalHeader.setProductLine(productLine);
        proposalHeader.setStatus(ReportStatus.DRAFT);
        proposalHeader.setDetails(createDetail(reportRequest.getListDetail(), proposalHeader));
        defectProposalRepository.save(proposalHeader);
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
    public DefectProposalUpdateResponse updateDefectProposal(Long id, DefectProposalRequest request) throws BadRequestException {
        DefectProposal proposal = defectProposalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Defect Proposal", "id", id));

        List<DefectProposalDetailRequest> items = request.getListDetail();
        if (items == null || items.isEmpty()) {
            throw new BusinessException("Cannot submit proposal without details");
        }
        // load existing details (of this proposal)
        List<DefectProposalDetail> existingDetails = defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(id);
        Map<Long, DefectProposalDetail> existingMap = new HashMap<>();
        for (DefectProposalDetail detail : existingDetails) {
            existingMap.put(detail.getId(), detail);
        }
        // validate ids belong to proposal
        for (DefectProposalDetailRequest item : items) {
            Long detailId = item.getId();
            if (detailId != null && !existingMap.containsKey(detailId)) {
                throw new BadRequestException("Detail id " + detailId + " does not belong to proposal " + proposal.getId());
            }
        }
        // dùng để xác định những detail nào còn tồn tại trong request
        Set<Long> requestDetailIds = new HashSet<>();
        //apply create/update/delete
        for (DefectProposalDetailRequest item : items) {
            // create
            if (item.getId() == null) {
                DefectProposalDetail newEntity = mapToEntity(item, proposal);
                defectProposalDetailRepository.save(newEntity);
                continue;
            }
            requestDetailIds.add(item.getId());
            // update existing
            DefectProposalDetail entity = existingMap.get(item.getId());
            if (entity == null) {
                throw new BadRequestException("Detail id " + item.getId() + " does not belong to this proposal");
            }
            if (item.getDefectId() != null) {
                Defect defectRef = defectRepository.getReferenceById(item.getDefectId());
                entity.setDefect(defectRef);
            } else {
                entity.setDefect(null);
            }
            if (item.getProcessId() == null) {
                throw new BadRequestException("processId is required");
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
            defectProposalDetailRepository.save(entity);
        }
        for (DefectProposalDetail existing : existingDetails) {
            if (!requestDetailIds.contains(existing.getId())) {
                existing.setDeleteFlag(true);
                defectProposalDetailRepository.save(existing);
            }
        }
        DefectProposal updatedProposal = defectProposalRepository.save(proposal);
        // Query lại & build response (đảm bảo trả về state mới nhất)
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
        DefectProposal proposal = defectProposalRepository.findById(proposalId).orElseThrow(() -> new ResourceNotFoundException("Defect Proposal", "id", proposalId));
        validateProposalForSubmission(proposal);
        approvalService.submit(proposal, currentUser, request);
        defectProposalRepository.save(proposal);
    }

    // Approval Methods
    @Override
    public void submit(Long proposalId, User currentUser, HttpServletRequest request) {
        DefectProposal proposal = defectProposalRepository.findById(proposalId).orElseThrow(() -> new ResourceNotFoundException("Defect Proposal", "id", proposalId));
        if (!proposal.getCreatedBy().equals(currentUser.getUsername())) {
            throw new BusinessException("Only author can submit this proposal");
        }
        approvalService.submit(proposal, currentUser, request);
        defectProposalRepository.save(proposal);
    }

    @Override
    public void approve(Long proposalId, User currentUser, ApproveRequest req, HttpServletRequest request) {
        DefectProposal proposal = defectProposalRepository.findById(proposalId).orElseThrow(() -> new ResourceNotFoundException("Defect Proposal", "id", proposalId));
        approvalService.approve(proposal, currentUser, req, request);
        defectProposalRepository.save(proposal);
    }

    @Override
    public void reject(Long proposalId, User currentUser, RejectRequest req, HttpServletRequest request) {
        DefectProposal proposal = defectProposalRepository.findById(proposalId).orElseThrow(() -> new ResourceNotFoundException("Defect Proposal", "id", proposalId));
        approvalService.reject(proposal, currentUser, req, request);
        defectProposalRepository.save(proposal);
    }

    @Override
    public boolean canApprove(Long proposalId, User currentUser) {
        DefectProposal proposal = defectProposalRepository.findById(proposalId).orElseThrow(() -> new ResourceNotFoundException("Defect Proposal", "id", proposalId));
        return approvalService.canApprove(proposal, currentUser);
    }

    @Override
    public void revise(Long id, User currentUser, HttpServletRequest request) {
        DefectProposal proposal = defectProposalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Defect Proposal not found"));
        if (!proposal.getCreatedBy().equals(proposal.getCreatedBy())) {
            throw new BusinessException("Only author can edit on this proposal");
        }
        approvalService.revise(proposal, currentUser, request);
    }


    private List<DefectProposalDetail> createDetail(List<DefectProposalDetailRequest> DefectProposalDetailList, DefectProposal proposal) {
        List<DefectProposalDetail> details = new ArrayList<>();
        for (DefectProposalDetailRequest detailRequest : DefectProposalDetailList) {
            Process process = processRepository.findById(detailRequest.getProcessId()).orElseThrow(() -> new EntityNotFoundException("Process not found"));
            DefectProposalDetail entity = new DefectProposalDetail();
            entity.setDefectProposal(proposal);
            if (detailRequest.getDefectId() != null) {
                Defect defect = defectRepository.findById(detailRequest.getDefectId()).orElse(null);
                entity.setDefect(defect);
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
            details.add(entity);
        }
        return details;
    }

    private DefectProposalDetail mapToEntity(DefectProposalDetailRequest request, DefectProposal proposal) {

        if (request == null) {
            throw new IllegalArgumentException("Request must not be null");
        }
        if (request.getProcessId() == null) {
            throw new IllegalArgumentException("processId is required");
        }

        if (request.getProposalType() == null) {
            throw new IllegalArgumentException("proposalType is required");
        }

        if (request.getDefectDescription() == null || request.getDefectDescription().isBlank()) {
            throw new IllegalArgumentException("defectDescription is required");
        }

        if (request.getDetectedDate() == null) {
            throw new IllegalArgumentException("detectedDate is required");
        }

        DefectProposalDetail entity = new DefectProposalDetail();
        entity.setDefectProposal(proposal);

        if (request.getDefectId() != null) {
            Defect defect = defectRepository.getReferenceById(request.getDefectId());
            entity.setDefect(defect);
        } else {
            entity.setDefect(null);
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

        entity.setDeleteFlag(false);

        return entity;
    }

    private DefectProposalDetailUpdateResponse mapToResponse(DefectProposalDetail entity) {
        if (entity == null) {
            return null;
        }

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

    private void validateProposalForSubmission(DefectProposal proposal) throws BusinessException {
        if (proposal.getDetails() == null || proposal.getDetails().isEmpty()) {
            throw new BusinessException("Cannot submit proposal without details");
        }
        for (DefectProposalDetail detail : proposal.getDetails()) {
            if (detail.getProcess() == null) {
                throw new BusinessException("Process is required for all details");
            }
            if (detail.getProposalType() == null) {
                throw new BusinessException("Proposal type is required for all details");
            }
            if (detail.getDefectDescription() == null || detail.getDefectDescription().isBlank()) {
                throw new BusinessException("Defect description is required for all details");
            }
            if (detail.getDetectedDate() == null) {
                throw new BusinessException("Detected date is required for all details");
            }
        }
    }

}
