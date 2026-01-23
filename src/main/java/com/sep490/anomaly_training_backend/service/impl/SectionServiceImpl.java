package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.SectionRequest;
import com.sep490.anomaly_training_backend.dto.response.SectionResponse;
import com.sep490.anomaly_training_backend.model.Section;
import com.sep490.anomaly_training_backend.mapper.SectionMapper;
import com.sep490.anomaly_training_backend.repository.SectionRepository;
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

    @Override
    @Transactional
    public SectionResponse createSection(SectionRequest request) {
        // Validate tên trùng (tuỳ chọn)
        if (sectionRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên bộ phận '" + request.getName() + "' đã tồn tại.");
        }

        // Mapper tự lo việc tìm User Manager
        Section section = sectionMapper.toEntity(request);

        return sectionMapper.toDTO(sectionRepository.save(section));
    }

    @Override
    @Transactional
    public SectionResponse updateSection(Long id, SectionRequest request) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Section với ID: " + id));

        // Update fields
        sectionMapper.updateEntity(section, request);

        return sectionMapper.toDTO(sectionRepository.save(section));
    }

    @Override
    @Transactional
    public void deleteSection(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Section với ID: " + id));

        // Soft Delete
        section.setDeleteFlag(true);
        sectionRepository.save(section);
    }

    @Override
    public SectionResponse getSectionById(Long id) {
        return sectionRepository.findById(id)
                .filter(s -> !s.isDeleteFlag()) // Chỉ lấy cái chưa xoá
                .map(sectionMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Section hoặc đã bị xoá. ID: " + id));
    }

    @Override
    public List<SectionResponse> getAllSections() {
        return sectionRepository.findAll().stream()
                .filter(s -> !s.isDeleteFlag())
                .map(sectionMapper::toDTO)
                .collect(Collectors.toList());
    }
}