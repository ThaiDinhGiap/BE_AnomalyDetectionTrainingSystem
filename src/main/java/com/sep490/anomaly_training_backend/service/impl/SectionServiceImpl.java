package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.SectionRequest;
import com.sep490.anomaly_training_backend.dto.response.ProductLineResponse;
import com.sep490.anomaly_training_backend.dto.response.SectionResponse;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.ProductLineMapper;
import com.sep490.anomaly_training_backend.mapper.SectionMapper;
import com.sep490.anomaly_training_backend.model.Section;
import com.sep490.anomaly_training_backend.repository.ProductLineRepository;
import com.sep490.anomaly_training_backend.repository.SectionRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final SectionMapper sectionMapper;
    private final ProductLineRepository productLineRepository;
    private final ProductLineMapper productLineMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SectionResponse createSection(SectionRequest request) {
        if (sectionRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.SECTION_NAME_ALREADY_EXISTS);
        }

        Section section = sectionMapper.toEntity(request);
        return sectionMapper.toDTO(sectionRepository.save(section));
    }

    @Override
    @Transactional
    public SectionResponse updateSection(Long id, SectionRequest request) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SECTION_NOT_FOUND));

        section.setName(request.getName());
        section.setCode(request.getCode());
        section.setManager(userRepository.findById(request.getManagerId()).orElse(null));

        sectionRepository.save(section);

        return sectionMapper.toDTO(sectionRepository.save(section));
    }

    @Override
    @Transactional
    public void deleteSection(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SECTION_NOT_FOUND));

        section.setDeleteFlag(true);
        sectionRepository.save(section);
    }

    @Override
    public SectionResponse getSectionById(Long id) {
        return sectionRepository.findById(id)
                .filter(s -> !s.isDeleteFlag())
                .map(sectionMapper::toDTO)
                .orElseThrow(() -> new AppException(ErrorCode.SECTION_NOT_FOUND));
    }

    @Override
    public List<SectionResponse> getAllSections() {
        return sectionRepository.findAll().stream()
                .filter(s -> !s.isDeleteFlag())
                .map(this::enrichDto)
                .collect(Collectors.toList());
    }

    private SectionResponse enrichDto(Section section) {
        SectionResponse sectionResponse = sectionMapper.toDTO(section);
        List<ProductLineResponse> productLineResponse = productLineRepository.findBySection(section.getId())
                .stream()
                .map(productLineMapper::toDto)
                .toList();
        sectionResponse.setProductLines(productLineResponse);
        return sectionResponse;
    }
}