package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.CreateDefectProposalDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.CreateDefectProposalRequest;
import com.sep490.anomaly_training_backend.dto.response.DefectProposalResponse;
import com.sep490.anomaly_training_backend.mapper.DefectProposalMapper;
import com.sep490.anomaly_training_backend.model.Defect;
import com.sep490.anomaly_training_backend.model.DefectProposal;
import com.sep490.anomaly_training_backend.model.DefectProposalDetail;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.DefectProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefectProposalServiceImpl implements DefectProposalService {
    private final DefectProposalRepository DefectProposalRepository;
    private final DefectRepository defectRepository;
    private final DefectProposalMapper defectProposalMapper;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ProcessRepository processRepository;
    private final DefectProposalDetailRepository DefectProposalDetailRepository;

    @Override
    public List<DefectProposalResponse> getDefectProposalByTeamLeadAndGroup(Long id, String username) {
        List<DefectProposalResponse> result = new ArrayList<>();
//        List<DefectProposal> listEntity = DefectProposalRepository.findByGroupIdAndCreatedByAndDeleteFlagFalse(id, username);
//        for (DefectProposal entity : listEntity) {
//            result.add(DefectProposalMapper.toResponse(entity, userRepository));
//        }
        return result;
    }

    @Override
    public void createDefectProposal(CreateDefectProposalRequest reportRequest) {
        Group group = groupRepository.findById(reportRequest.getGroupId()).get();
        DefectProposal report = new DefectProposal();
//        report.setProductLine();
//        report.setStatus(ReportStatus.DRAFT);
        DefectProposal mewReport = DefectProposalRepository.save(report);
        createDefectProposalDetailRequest(reportRequest.getDefectProposalDetail(), mewReport);
    }

    private void createDefectProposalDetailRequest(List<CreateDefectProposalDetailRequest> DefectProposalDetailList, DefectProposal report) {
        for(CreateDefectProposalDetailRequest detailRequest : DefectProposalDetailList) {
            Process process = processRepository.findById(detailRequest.getProcessId()).orElse(null);
            DefectProposalDetail  entity = new DefectProposalDetail();
            entity.setDefectProposal(report);
            if(detailRequest.getDefectId()!=null) {
                Defect defect = defectRepository.findById(detailRequest.getDefectId()).orElse(null);
                entity.setDefect(defect);
            }
//            entity.setProposalType(detailRequest.getProposalType());
            entity.setDefectDescription(detailRequest.getDefectDescription());
            entity.setProcess(process);
            entity.setDetectedDate(detailRequest.getDetectedDate());
            entity.setNote(detailRequest.getNote());
            DefectProposalDetailRepository.save(entity);
        }
    }

}
