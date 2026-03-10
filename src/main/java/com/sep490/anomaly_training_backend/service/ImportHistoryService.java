package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.response.ImportErrorItem;
import com.sep490.anomaly_training_backend.enums.ImportStatus;
import com.sep490.anomaly_training_backend.enums.ImportType;
import com.sep490.anomaly_training_backend.model.User;

import java.util.List;

public interface ImportHistoryService {
     void saveHistory(User user, String filePath, ImportType importType, ImportStatus status, List<ImportErrorItem> errors);
}
