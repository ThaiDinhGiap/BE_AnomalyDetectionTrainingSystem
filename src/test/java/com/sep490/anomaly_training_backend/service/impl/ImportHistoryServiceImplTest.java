package com.sep490.anomaly_training_backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import com.sep490.anomaly_training_backend.dto.response.ImportHistoryResponse;
import com.sep490.anomaly_training_backend.enums.ImportStatus;
import com.sep490.anomaly_training_backend.enums.ImportType;
import com.sep490.anomaly_training_backend.mapper.ImportHistoryMapper;
import com.sep490.anomaly_training_backend.model.ImportHistory;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.ImportHistoryRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportHistoryServiceImplTest {

    @Mock
    private ImportHistoryRepository importHistoryRepository;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ImportHistoryMapper importHistoryMapper;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ImportHistoryServiceImpl importHistoryService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
    }

    @Test
    void saveHistory_WithErrors_ShouldSaveAsJson() throws JsonProcessingException {
        List<ImportErrorItem> errors = List.of(new ImportErrorItem(1, "code", "errVal", "Error"));
        when(objectMapper.writeValueAsString(errors)).thenReturn("[{\"line\":1,\"message\":\"Error\"}]");

        importHistoryService.saveHistory(user, "test.xlsx", ImportType.DEFECT_IMPORT, ImportStatus.FAIL, errors);

        verify(importHistoryRepository).save(argThat(history -> 
            history.getImportErrorDescription().contains("Error") &&
            history.getFilePath().equals("test.xlsx") &&
            history.getImportType() == ImportType.DEFECT_IMPORT
        ));
    }

    @Test
    void saveHistory_WithErrors_JsonException_ShouldSaveFallback() throws JsonProcessingException {
        List<ImportErrorItem> errors = List.of(new ImportErrorItem(1, "code", "errVal", "Error"));
        when(objectMapper.writeValueAsString(errors)).thenThrow(new JsonProcessingException("Test failure") {});

        importHistoryService.saveHistory(user, "test.xlsx", ImportType.DEFECT_IMPORT, ImportStatus.FAIL, errors);

        verify(importHistoryRepository).save(argThat(history -> 
            history.getImportErrorDescription().equals("[{\"message\":\"Cannot serialize import errors\"}]")
        ));
    }

    @Test
    void getHistory_ShouldReturnMappedDtos() {
        ImportHistory history = new ImportHistory();
        history.setId(10L);
        when(importHistoryRepository.findByUserIdAndImportTypeOrderByCreatedAtDesc(1L, ImportType.DEFECT_IMPORT))
                .thenReturn(List.of(history));
                
        ImportHistoryResponse response = mock(ImportHistoryResponse.class);
        when(response.getId()).thenReturn(10L);
        when(importHistoryMapper.toDto(history)).thenReturn(response);

        List<ImportHistoryResponse> result = importHistoryService.getHistory(user, "DEFECT_IMPORT");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
    }
}
