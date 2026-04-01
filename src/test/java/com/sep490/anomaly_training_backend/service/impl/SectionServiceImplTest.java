package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.SectionRequest;
import com.sep490.anomaly_training_backend.dto.response.ProductLineResponse;
import com.sep490.anomaly_training_backend.dto.response.SectionResponse;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.mapper.ProductLineMapper;
import com.sep490.anomaly_training_backend.mapper.SectionMapper;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.Section;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.ProductLineRepository;
import com.sep490.anomaly_training_backend.repository.SectionRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SectionServiceImplTest {

    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private SectionMapper sectionMapper;
    @Mock
    private ProductLineRepository productLineRepository;
    @Mock
    private ProductLineMapper productLineMapper;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SectionServiceImpl sectionService;

    private Section section;
    private SectionRequest request;
    private User manager;

    @BeforeEach
    void setUp() {
        manager = new User();
        manager.setId(1L);

        section = new Section();
        section.setId(10L);
        section.setName("SEC-A");
        section.setCode("SC001");

        request = new SectionRequest();
        request.setName("SEC-A-NEW");
        request.setCode("SC002");
        request.setManagerId(1L);
    }

    @Test
    void createSection_WhenNameExists_ShouldThrow() {
        when(sectionRepository.existsByName(request.getName())).thenReturn(true);

        assertThatThrownBy(() -> sectionService.createSection(request))
                .isInstanceOf(AppException.class);
    }

    @Test
    void createSection_Success() {
        when(sectionRepository.existsByName(request.getName())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(manager));
        when(sectionRepository.save(any(Section.class))).thenReturn(section);
        
        SectionResponse response = new SectionResponse();
        when(sectionMapper.toDTO(section)).thenReturn(response);

        SectionResponse result = sectionService.createSection(request);

        assertThat(result).isNotNull();
        verify(sectionRepository).save(any(Section.class));
    }

    @Test
    void updateSection_WhenNotFound_ShouldThrow() {
        when(sectionRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.updateSection(10L, request))
                .isInstanceOf(AppException.class);
    }

    @Test
    void updateSection_Success() {
        when(sectionRepository.findById(10L)).thenReturn(Optional.of(section));
        when(userRepository.findById(1L)).thenReturn(Optional.of(manager));
        when(sectionRepository.save(section)).thenReturn(section);
        
        SectionResponse response = new SectionResponse();
        when(sectionMapper.toDTO(section)).thenReturn(response);

        SectionResponse result = sectionService.updateSection(10L, request);

        assertThat(result).isNotNull();
        assertThat(section.getName()).isEqualTo("SEC-A-NEW");
        assertThat(section.getManager().getId()).isEqualTo(1L);
        verify(sectionRepository).save(section);
    }

    @Test
    void deleteSection_WhenNotFound_ShouldThrow() {
        when(sectionRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.deleteSection(10L))
                .isInstanceOf(AppException.class);
    }

    @Test
    void deleteSection_Success() {
        when(sectionRepository.findById(10L)).thenReturn(Optional.of(section));

        sectionService.deleteSection(10L);

        assertThat(section.isDeleteFlag()).isTrue();
        verify(sectionRepository).save(section);
    }

    @Test
    void getAllSections_ShouldReturnEnrichedList() {
        when(sectionRepository.findAll()).thenReturn(List.of(section));
        SectionResponse sResp = new SectionResponse();
        when(sectionMapper.toDTO(section)).thenReturn(sResp);

        ProductLine pl = new ProductLine();
        when(productLineRepository.findBySection(10L)).thenReturn(List.of(pl));
        
        when(productLineMapper.toDto(pl)).thenReturn(mock(ProductLineResponse.class));

        List<SectionResponse> results = sectionService.getAllSections();

        assertThat(results).hasSize(1);
        verify(productLineRepository).findBySection(10L);
    }
}
