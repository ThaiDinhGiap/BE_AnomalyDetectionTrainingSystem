package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.CreateDefectReportDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.CreateDefectReportRequest;
import com.sep490.anomaly_training_backend.dto.request.CreateTrainingTopicReportDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.CreateTrainingTopicReportRequest;
import com.sep490.anomaly_training_backend.dto.response.TrainingTopicReportResponse;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.mapper.TrainingTopicReportMapper;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.TrainingTopicReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class TrainingTopicReportServiceImpl implements TrainingTopicReportService {
    private final TrainingTopicReportRepository trainingTopicReportRepository;
    private final TrainingTopicReportDetailRepository trainingTopicReportDetailRepository;
    private final TrainingTopicRepository trainingTopicRepository;
    private final TrainingTopicReportMapper trainingTopicReportMapper;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final DefectRepository defectRepository;
    private final ProcessRepository processRepository;

    @Override
    public List<TrainingTopicReportResponse> getTrainingTopicReportsByTeamLeadAndGroup(Long id, String username) {
        List<TrainingTopicReport> entityList= trainingTopicReportRepository.findByGroupIdAndCreatedByAndDeleteFlagFalse(id, username);
        List<TrainingTopicReportResponse> trainingTopicReportResponses = new ArrayList<>();

        for (TrainingTopicReport entity : entityList) {
            trainingTopicReportResponses.add(trainingTopicReportMapper.toResponse(entity, userRepository));
        }

        return  trainingTopicReportResponses;
    }

    @Override
    public void createTrainingTopicReport(CreateTrainingTopicReportRequest reportRequest) {
        Group group = groupRepository.findById(reportRequest.getGroupId()).get();
        TrainingTopicReport report = new TrainingTopicReport();
        report.setGroup(group);
        report.setStatus(ReportStatus.DRAFT);
        TrainingTopicReport newReport = trainingTopicReportRepository.save(report);
        createTrainingTopicReportDetailRequest(reportRequest.getTrainingTopicReportDetail(), newReport);
    }


    private void createTrainingTopicReportDetailRequest(List<CreateTrainingTopicReportDetailRequest> reportDetailList, TrainingTopicReport report) {
        for(CreateTrainingTopicReportDetailRequest detailRequest : reportDetailList) {
            Process process = processRepository.findById(detailRequest.getProcessId()).orElse(null);
            TrainingTopicReportDetail entity = new TrainingTopicReportDetail();
            entity.setTrainingTopicReport(report);
            if (detailRequest.getTrainingTopicId() != null) {
                TrainingTopic trainingTopic = trainingTopicRepository.findById(detailRequest.getTrainingTopicId()).orElse(null);
                entity.setTrainingTopic(trainingTopic);
            }

            if (detailRequest.getDefectId()!=null) {
                Defect defect = defectRepository.findById(detailRequest.getDefectId()).orElse(null);
                entity.setDefect(defect);
            }
            entity.setReportType(detailRequest.getReportType());
            entity.setCategoryName(detailRequest.getCategoryName());
            entity.setProcess(process);
            entity.setTrainingDetail(detailRequest.getTrainingDetail());
            entity.setTrainingSample(detailRequest.getTrainingSample());
            entity.setNote(detailRequest.getNote());
            trainingTopicReportDetailRepository.save(entity);
        }
    }
}
