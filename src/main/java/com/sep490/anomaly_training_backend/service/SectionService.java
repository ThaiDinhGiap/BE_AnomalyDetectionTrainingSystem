package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.SectionRequest;
import com.sep490.anomaly_training_backend.dto.response.SectionResponse;

import java.util.List;

public interface SectionService {
    SectionResponse createSection(SectionRequest request);

    SectionResponse updateSection(Long id, SectionRequest request);

    void deleteSection(Long id);

//    SectionResponse getSectionById(Long id);

    List<SectionResponse> getAllSections();
}