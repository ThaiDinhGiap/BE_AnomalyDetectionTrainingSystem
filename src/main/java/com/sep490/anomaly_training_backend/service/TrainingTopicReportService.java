package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.response.TrainingTopicReportResponse;
import java.util.List;

public interface TrainingTopicReportService {

    List<TrainingTopicReportResponse> getTrainingTopicReportsByTeamLeadAndGroup(Long id, String username);

}
