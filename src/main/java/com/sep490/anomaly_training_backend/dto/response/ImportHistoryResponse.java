package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.ImportStatus;
import com.sep490.anomaly_training_backend.enums.ImportType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Builder
public class ImportHistoryResponse {
    private Long id;
    private LocalDateTime importDate;
    private Long userId;
    private String userName;
    private ImportStatus status;
    private String filePath;
    private ImportType importType;
    private String importErrorDescription;

}
