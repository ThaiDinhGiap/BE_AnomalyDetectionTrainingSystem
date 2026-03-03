package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.CreateTrainingSampleProposalDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.CreateTrainingSampleProposalRequest;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalResponse;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleProposalMapper;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.TrainingSampleProposalService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class TrainingSampleProposalServiceImpl implements TrainingSampleProposalService {
    private final TrainingSampleProposalRepository trainingSampleProposalRepository;
    private final TrainingSampleProposalDetailRepository TrainingSampleProposalDetailRepository;
    private final TrainingSampleRepository trainingSampleRepository;
    private final TrainingSampleProposalMapper trainingSampleProposalMapper;
    private final UserRepository userRepository;
    private final ProductLineRepository productLineRepository;
    private final DefectRepository defectRepository;
    private final ProcessRepository processRepository;

    @Override
    public List<TrainingSampleProposalResponse> getTrainingSampleProposalsByTeamLeadAndGroup(Long id, String username) {
        List<TrainingSampleProposal> entityList= trainingSampleProposalRepository.findByProductLineIdAndCreatedByAndDeleteFlagFalse(id, username);
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


    private void createTrainingSampleProposalDetailRequest(List<CreateTrainingSampleProposalDetailRequest> ProposalDetailList, TrainingSampleProposal Proposal) {
        for(CreateTrainingSampleProposalDetailRequest detailRequest : ProposalDetailList) {
            Process process = processRepository.findById(detailRequest.getProcessId()).orElse(null);
            TrainingSampleProposalDetail entity = new TrainingSampleProposalDetail();
            entity.setTrainingSampleProposal(Proposal);
            if(detailRequest.getTrainingSampleId()!=null) {
                TrainingSample trainingSample = trainingSampleRepository.findById(detailRequest.getTrainingSampleId()).orElse(null);
                entity.setTrainingSample(trainingSample);
            }
            if(detailRequest.getDefectId()!=null) {
                Defect defect = defectRepository.findById(detailRequest.getDefectId()).orElse(null);
                entity.setDefect(defect);
            }
            entity.setProposalType(detailRequest.getProposalType());
            entity.setCategoryName(detailRequest.getCategoryName());
            entity.setProcess(process);
            entity.setTrainingDescription(detailRequest.getTrainingDetail());
            entity.setTrainingDescription(detailRequest.getTrainingSample());
            entity.setNote(detailRequest.getNote());
            TrainingSampleProposalDetailRepository.save(entity);
        }
    }
}
