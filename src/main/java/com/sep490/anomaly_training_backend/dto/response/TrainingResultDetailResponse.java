package com.sep490.anomaly_training_backend.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class TrainingResultDetailResponse {
    private Long id;
    private String lineName;
    private String createdByName;

    private List<DetailRowDto> details;

    @Data
    public static class DetailRowDto {
        private Long id;

        private LocalDate actualDate;
        private String processName;
        private String classification;
        private BigDecimal standardTime;
        private String productCode;
        private String trainingSample;
        private String employeeName;
        private String employeeCode;

        private LocalTime timeIn;
        private LocalTime timeOut;
        private Boolean isPass;
        private String note;

        private String signatureProInName;
        private String signatureFiInName;
        private String signatureProOutName;
        private String signatureFiOutName;
    }
}