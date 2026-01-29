package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.DefectReportResponse;
import com.sep490.anomaly_training_backend.mapper.DefectReportMapper;
import com.sep490.anomaly_training_backend.model.DefectReport;
import com.sep490.anomaly_training_backend.repository.DefectReportRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.DefectReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefectReportServiceImpl implements DefectReportService {
    private final DefectReportRepository DefectReportRepository;
    private final DefectReportMapper defectReportMapper;
    private final UserRepository userRepository;

    @Override
    public List<DefectReportResponse> getDefectReportByTeamLeadAndGroup(Long id, String username) {
        List<DefectReportResponse> result = new ArrayList<>();
        List<DefectReport> listEntity = DefectReportRepository.findByGroupIdAndCreatedByAndDeleteFlagFalse(id, username);
        for (DefectReport entity : listEntity) {
            result.add(defectReportMapper.toResponse(entity, userRepository));
        }
        return result;
    }

}
