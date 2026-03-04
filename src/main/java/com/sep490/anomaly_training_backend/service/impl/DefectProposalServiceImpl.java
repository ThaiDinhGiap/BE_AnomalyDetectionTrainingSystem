package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.request.CreateDefectProposalDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.CreateDefectProposalRequest;
import com.sep490.anomaly_training_backend.dto.request.DefectProposalDetailUpdateRequest;
import com.sep490.anomaly_training_backend.dto.request.DefectProposalUpdateRequest;
import com.sep490.anomaly_training_backend.dto.request.RejectRequest;
import com.sep490.anomaly_training_backend.dto.response.DefectProposalDetailUpdateResponse;
import com.sep490.anomaly_training_backend.dto.response.DefectProposalResponse;
import com.sep490.anomaly_training_backend.dto.response.DefectProposalUpdateResponse;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.BusinessException;
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
import java.util.List;
import java.util.Map;

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
    public void createDefectProposalDraft(CreateDefectProposalRequest reportRequest) {
        ProductLine productLine = productLineRepository.findById(reportRequest.getProductLineId()).get();
        DefectProposal report = new DefectProposal();
        report.setProductLine(productLine);
        report.setStatus(ReportStatus.DRAFT);
        DefectProposal mewReport = defectProposalRepository.save(report);
        createDefectProposalDetailRequest(reportRequest.getDefectProposalDetail(), mewReport);
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
    public DefectProposalUpdateResponse updateDefectProposal(Long id, DefectProposalUpdateRequest request) throws BadRequestException {
        DefectProposal proposal = defectProposalRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Defect Proposal not found"));
        List<DefectProposalDetailUpdateRequest> items = request.getListUpdatedItems();
        if (items == null || items.isEmpty()) {
            throw new BadRequestException("Detail list must not be empty");
        }
        // load existing details (of this proposal)
        List<DefectProposalDetail> existingDetails =
                defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(id);

        Map<Long, DefectProposalDetail> existingMap = new HashMap<>();
        for (DefectProposalDetail detail : existingDetails) {
            existingMap.put(detail.getId(), detail);
        }
        // validate ids belong to proposal
        for (DefectProposalDetailUpdateRequest item : items) {
            Long detailId = item.getId();
            if (detailId != null && !existingMap.containsKey(detailId)) {
                throw new BadRequestException("Detail id " + detailId + " does not belong to proposal " + proposal.getId());
            }
        }
        //apply create/update/delete
        for (DefectProposalDetailUpdateRequest item : items) {
            // create
            if (item.getId() == null) {
                DefectProposalDetail newEntity = mapToEntity(item, proposal);
                defectProposalDetailRepository.save(newEntity);
                continue;
            }
            // update or delete existing
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
            entity.setIsEscaped(item.getIsEscaped());
            entity.setNote(item.getNote());
            entity.setOriginCause(item.getOriginCause());
            entity.setOutflowCause(item.getOutflowCause());
            entity.setCausePoint(item.getCausePoint());
            entity.setDeleteFlag(item.getDeleteFlag() != null && item.getDeleteFlag());
            defectProposalDetailRepository.save(entity);
        }
        defectProposalRepository.save(proposal);
        // Query lại & build response (đảm bảo trả về state mới nhất)
        List<DefectProposalDetail> latestDetails =
                defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(id);

        DefectProposalUpdateResponse response = new DefectProposalUpdateResponse();
        response.setId(proposal.getId());
        response.setProductLineId(proposal.getProductLine() != null ? proposal.getProductLine().getId() : null);

        List<DefectProposalDetailUpdateResponse> detailResponses = new ArrayList<>();
        for (DefectProposalDetail detail : latestDetails) {
            detailResponses.add(mapToResponse(detail));
        }
        response.setDefectProposalDetail(detailResponses);
        return response;
    }

    // Approval Methods
    @Override
    public void submit(Long proposalId, User currentUser, HttpServletRequest request) {
        DefectProposal proposal = defectProposalRepository.findById(proposalId).orElseThrow(() -> new EntityNotFoundException("Defect Proposal not found"));
        if (!proposal.getCreatedBy().equals(currentUser.getUsername())) {
            throw new BusinessException("Only author can submit this proposal");
        }
        approvalService.submit(proposal, currentUser, request);
        defectProposalRepository.save(proposal);
    }

    @Override
    public void approve(Long proposalId, User currentUser, ApproveRequest req, HttpServletRequest request) {
        DefectProposal proposal = defectProposalRepository.findById(proposalId).orElseThrow(() -> new EntityNotFoundException("Defect Proposal not found"));
        approvalService.approve(proposal, currentUser, req, request);
        defectProposalRepository.save(proposal);
    }

    @Override
    public void reject(Long proposalId, User currentUser, RejectRequest req, HttpServletRequest request) {
        DefectProposal proposal = defectProposalRepository.findById(proposalId).orElseThrow(() -> new EntityNotFoundException("Defect Proposal not found"));
        approvalService.reject(proposal, currentUser, req, request);
        defectProposalRepository.save(proposal);
    }

    @Override
    public boolean canApprove(Long proposalId, User currentUser) {
        DefectProposal proposal = defectProposalRepository.findById(proposalId).orElseThrow(() -> new EntityNotFoundException("Defect Proposal not found"));
        return approvalService.canApprove(proposal, currentUser);
    }

    @Override
    public void revise(Long id, User currentUser, HttpServletRequest request) {
        DefectProposal proposal = defectProposalRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Defect Proposal not found"));
        if (!proposal.getCreatedBy().equals(proposal.getCreatedBy())) {
            throw new BusinessException("Only author can edit on this proposal");
        }
        approvalService.revise(proposal, currentUser, request);
    }


    private void createDefectProposalDetailRequest(List<CreateDefectProposalDetailRequest> DefectProposalDetailList, DefectProposal proposal) {
        for (CreateDefectProposalDetailRequest detailRequest : DefectProposalDetailList) {
            Process process = processRepository.findById(detailRequest.getProcessId()).orElse(null);
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
            defectProposalDetailRepository.save(entity);
        }
    }

    private DefectProposalDetail mapToEntity(DefectProposalDetailUpdateRequest request, DefectProposal proposal) {

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

        Boolean escaped = request.getIsEscaped();
        entity.setIsEscaped(escaped != null && escaped);

        entity.setNote(request.getNote());
        entity.setOriginCause(request.getOriginCause());
        entity.setOutflowCause(request.getOutflowCause());
        entity.setCausePoint(request.getCausePoint());

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
        }

        response.setDetectedDate(entity.getDetectedDate());
        response.setIsEscaped(entity.getIsEscaped());

        response.setNote(entity.getNote());
        response.setOriginCause(entity.getOriginCause());
        response.setOutflowCause(entity.getOutflowCause());
        response.setCausePoint(entity.getCausePoint());
        response.setDeleteFlag(entity.isDeleteFlag());
        return response;
    }

}
