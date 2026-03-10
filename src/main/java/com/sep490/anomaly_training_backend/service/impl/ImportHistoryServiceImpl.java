package com.sep490.anomaly_training_backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import com.sep490.anomaly_training_backend.enums.ImportStatus;
import com.sep490.anomaly_training_backend.enums.ImportType;
import com.sep490.anomaly_training_backend.model.ImportHistory;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.ImportHistoryRepository;
import com.sep490.anomaly_training_backend.service.ImportHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportHistoryServiceImpl implements ImportHistoryService {
    private final ImportHistoryRepository importHistoryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void saveHistory(User user, String filePath, ImportType importType, ImportStatus status, List<ImportErrorItem> errors) {
        String errorJson = null;

        try {
            if (errors != null && !errors.isEmpty()) {
                errorJson = objectMapper.writeValueAsString(errors);
            }
        } catch (JsonProcessingException e) {
            errorJson = "[{\"message\":\"Cannot serialize import errors\"}]";
        }

        ImportHistory history = ImportHistory.builder()
                .importDate(LocalDateTime.now())
                .user(user)
                .status(status)
                .filePath(filePath)
                .importType(importType)
                .importErrorDescription(errorJson)
                .build();

        importHistoryRepository.save(history);
    }
}
