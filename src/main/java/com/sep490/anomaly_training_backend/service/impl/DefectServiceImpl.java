package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.DefectResponse;
import com.sep490.anomaly_training_backend.mapper.DefectMapper;
import com.sep490.anomaly_training_backend.model.Defect;
import com.sep490.anomaly_training_backend.repository.DefectRepository;
import com.sep490.anomaly_training_backend.service.DefectService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefectServiceImpl implements DefectService {

    private final DefectRepository defectRepository;
    private final DefectMapper defectMapper;

    @Override
    public List<DefectResponse> getDefectBySupervisor(Long supervisorId) {
        return defectRepository.findAllBySupervisorAndDeleteFlagFalse(supervisorId)
                               .stream()
                               .map(defectMapper::toDto).toList();
    }

    @Override
    public List<DefectResponse> getDefectByProductLine(Long productLineId) {
        return defectRepository.findAllByProductLineAndDeleteFlagFalse(productLineId)
                .stream()
                .map(defectMapper::toDto).toList();
    }

    @Override
    public DefectResponse getDefectById(Long id) {
        Defect defect = defectRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Defect not found"));
        return defectMapper.toDto(defect);
    }

}
