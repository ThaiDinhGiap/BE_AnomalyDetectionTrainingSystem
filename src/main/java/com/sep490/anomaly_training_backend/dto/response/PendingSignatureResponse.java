package com.sep490.anomaly_training_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PendingSignatureResponse {
    private long count;
    private String signatureType; // PRO_OUT, FI_OUT, SV
    private String description;
    private List<Long> resultIds;
    private List<String> employeeCodes;
}
