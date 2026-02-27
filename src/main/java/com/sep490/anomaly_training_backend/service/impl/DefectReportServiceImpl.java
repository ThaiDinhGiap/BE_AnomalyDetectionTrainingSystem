package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.CreateDefectReportDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.CreateDefectReportRequest;
import com.sep490.anomaly_training_backend.dto.response.DefectReportResponse;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.mapper.DefectReportMapper;
import com.sep490.anomaly_training_backend.model.Defect;
import com.sep490.anomaly_training_backend.model.DefectProposal;
import com.sep490.anomaly_training_backend.model.DefectProposalDetail;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.DefectReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefectReportServiceImpl implements DefectReportService {
    private final DefectProposalRepository defectReportRepository;
    private final DefectRepository defectRepository;
    private final DefectReportMapper defectReportMapper;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ProcessRepository processRepository;
    private final DefectProposalDetailRepository defectReportDetailRepository;

    @Override
    public List<DefectReportResponse> getDefectReportByTeamLeadAndGroup(Long id, String username) {
        List<DefectReportResponse> result = new ArrayList<>();
//        List<DefectProposal> listEntity = defectReportRepository.findByGroupIdAndCreatedByAndDeleteFlagFalse(id, username);
//        for (DefectProposal entity : listEntity) {
//            result.add(defectReportMapper.toResponse(entity, userRepository));
//        }
        return result;
    }

    @Override
    public void createDefectReport(CreateDefectReportRequest reportRequest) {
        Group group = groupRepository.findById(reportRequest.getGroupId()).get();
        DefectProposal report = new DefectProposal();
//        report.setProductLine();
//        report.setStatus(ReportStatus.DRAFT);
        DefectProposal mewReport = defectReportRepository.save(report);
        createDefectReportDetailRequest(reportRequest.getDefectReportDetail(), mewReport);
    }

    private void createDefectReportDetailRequest(List<CreateDefectReportDetailRequest> defectReportDetailList, DefectProposal report) {
        for(CreateDefectReportDetailRequest detailRequest : defectReportDetailList) {
            Process process = processRepository.findById(detailRequest.getProcessId()).orElse(null);
            DefectProposalDetail  entity = new DefectProposalDetail();
            entity.setDefectProposal(report);
            if(detailRequest.getDefectId()!=null) {
                Defect defect = defectRepository.findById(detailRequest.getDefectId()).orElse(null);
                entity.setDefect(defect);
            }
//            entity.setProposalType(detailRequest.getReportType());
            entity.setDefectDescription(detailRequest.getDefectDescription());
            entity.setProcess(process);
            entity.setDetectedDate(detailRequest.getDetectedDate());
            entity.setNote(detailRequest.getNote());
            defectReportDetailRepository.save(entity);
        }
    }

}
