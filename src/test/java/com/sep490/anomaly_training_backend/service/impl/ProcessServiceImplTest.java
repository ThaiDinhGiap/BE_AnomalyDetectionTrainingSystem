package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.ProcessRequest;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.mapper.ProcessMapper;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.ProcessRepository;
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
class ProcessServiceImplTest {

    @Mock
    private ProcessRepository processRepository;
    @Mock
    private ProcessMapper processMapper;

    @InjectMocks
    private ProcessServiceImpl processService;

    private Process process;
    private ProcessRequest request;

    @BeforeEach
    void setUp() {
        process = new Process();
        process.setId(1L);
        process.setCode("PRC-001");

        request = new ProcessRequest();
        request.setCode("PRC-001");
    }

    @Test
    void createProcess_WhenCodeExists_ShouldThrow() {
        when(processRepository.existsByCode("PRC-001")).thenReturn(true);

        assertThatThrownBy(() -> processService.createProcess(request))
                .isInstanceOf(AppException.class);
    }

    @Test
    void createProcess_ShouldSaveAndReturnResponse() {
        when(processRepository.existsByCode("PRC-001")).thenReturn(false);
        when(processMapper.toEntity(request)).thenReturn(process);
        when(processRepository.save(process)).thenReturn(process);
        
        ProcessResponse response = new ProcessResponse();
        when(processMapper.toDTO(process)).thenReturn(response);

        ProcessResponse result = processService.createProcess(request);

        assertThat(result).isNotNull();
        verify(processRepository).save(process);
    }

    @Test
    void updateProcessByAdmin_WhenNotFound_ShouldThrow() {
        when(processRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> processService.updateProcessByAdmin(1L, request))
                .isInstanceOf(AppException.class);
    }

    @Test
    void updateProcessByAdmin_WhenCodeChangedAndExists_ShouldThrow() {
        Process anotherProcess = new Process();
        anotherProcess.setId(1L);
        anotherProcess.setCode("OLD-CODE");

        when(processRepository.findById(1L)).thenReturn(Optional.of(anotherProcess));
        when(processRepository.existsByCode("PRC-001")).thenReturn(true);

        assertThatThrownBy(() -> processService.updateProcessByAdmin(1L, request))
                .isInstanceOf(AppException.class);
    }

    @Test
    void updateProcessByAdmin_ShouldUpdateAndSave() {
        when(processRepository.findById(1L)).thenReturn(Optional.of(process));
        doNothing().when(processMapper).updateEntity(process, request);
        when(processRepository.save(process)).thenReturn(process);
        ProcessResponse response = new ProcessResponse();
        when(processMapper.toDTO(process)).thenReturn(response);

        ProcessResponse result = processService.updateProcessByAdmin(1L, request);

        assertThat(result).isNotNull();
        verify(processMapper).updateEntity(process, request);
        verify(processRepository).save(process);
    }

    @Test
    void deleteProcess_WhenNotFound_ShouldThrow() {
        when(processRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> processService.deleteProcess(1L))
                .isInstanceOf(AppException.class);
    }

    @Test
    void deleteProcess_ShouldSoftDelete() {
        when(processRepository.findById(1L)).thenReturn(Optional.of(process));

        processService.deleteProcess(1L);

        assertThat(process.isDeleteFlag()).isTrue();
        verify(processRepository).saveAndFlush(process);
    }

    @Test
    void getProcessesByProductLineId_ShouldReturnList() {
        when(processRepository.findByProductLineId(10L)).thenReturn(List.of(process));
        ProcessResponse response = new ProcessResponse();
        when(processMapper.toDTO(process)).thenReturn(response);

        List<ProcessResponse> results = processService.getProcessesByProductLineId(10L);

        assertThat(results).hasSize(1);
        verify(processMapper).toDTO(process);
    }

    @Test
    void getProcessesByProductLineId_ShouldFilterDeleted() {
        process.setDeleteFlag(true);
        when(processRepository.findByProductLineId(10L)).thenReturn(List.of(process));

        List<ProcessResponse> results = processService.getProcessesByProductLineId(10L);

        assertThat(results).isEmpty();
        verify(processMapper, never()).toDTO(any());
    }
}
