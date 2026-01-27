package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.DefectResponse;
import com.sep490.anomaly_training_backend.mapper.DefectMapper;
import com.sep490.anomaly_training_backend.repository.DefectRepository;
import com.sep490.anomaly_training_backend.service.DefectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefectServiceImpl implements DefectService {

    private final DefectRepository defectRepository;
    private final DefectMapper defectMapper;

    public List<DefectResponse> getDefects() {
        return defectRepository.findByDeleteFlagFalse().stream().map(defectMapper::toDto).toList();
    }

    @Override
    public List<DefectResponse> getDefectByProcess(Long processId) {
        return defectRepository.findByProcessIdAndDeleteFlagFalse(processId).stream().map(defectMapper::toDto).toList();
    }
}
