package com.sep490.anomaly_training_backend.dto.request;

import lombok.Data;

import java.util.List;

/**
 * Filter criteria for export list endpoints.
 * All fields are optional. When null, the corresponding filter is not applied.
 * Sending an empty body {} exports all records (backward compatible).
 */
@Data
public class ExportFilterRequest {
    private List<Long> ids;
}
