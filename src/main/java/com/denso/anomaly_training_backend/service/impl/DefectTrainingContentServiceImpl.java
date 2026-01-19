package com.denso.anomaly_training_backend.service.impl;

import com.denso.anomaly_training_backend.dto.response.DefectTrainingContentResponse;
import com.denso.anomaly_training_backend.mapper.DefectTrainingContentMapper;
import com.denso.anomaly_training_backend.repository.DefectTrainingContentRepository;
import com.denso.anomaly_training_backend.service.DefectTrainingContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefectTrainingContentServiceImpl implements DefectTrainingContentService {
    private final DefectTrainingContentRepository defectTrainingContentRepository;

    @Override
    public List<DefectTrainingContentResponse> getAllDefectTrainingContent() {
        return defectTrainingContentRepository.findAll()
                                              .stream()
                                              .map(DefectTrainingContentMapper.INSTANCE::toResponse)
                                              .toList();
    }

    @Override
    public Page<DefectTrainingContentResponse> getAll(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        return defectTrainingContentRepository.findAll(pageable)
                .map(DefectTrainingContentMapper.INSTANCE::toResponse);
    }
}
