package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.DefectReportDetailResponse;
import com.sep490.anomaly_training_backend.mapper.DefectReportDetailMapper;
import com.sep490.anomaly_training_backend.repository.DefectReportDetailRepository;
import com.sep490.anomaly_training_backend.service.DefectReportDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefectReportDetailServiceImpl implements DefectReportDetailService {
    private final DefectReportDetailRepository defectReportDetailRepository;
    private final DefectReportDetailMapper defectReportDetailMapper;

    @Override
    public List<DefectReportDetailResponse> getDefectReportDetails(Long defectReportId) {
        return defectReportDetailRepository.findByDefectReportIdAndDeleteFlagFalse(defectReportId)
                                           .stream()
                                           .map(defectReportDetailMapper::toResponse).toList();
    }
}
