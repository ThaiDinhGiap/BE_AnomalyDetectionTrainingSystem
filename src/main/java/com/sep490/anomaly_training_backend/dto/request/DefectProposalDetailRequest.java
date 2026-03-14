package com.sep490.anomaly_training_backend.dto.request;

import com.sep490.anomaly_training_backend.enums.ProposalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectProposalDetailRequest {
    Long id;
    Long defectId;
    ProposalType proposalType;
    String defectDescription;
    Long processId;
    LocalDate detectedDate;
    String note;
    String originCause;
    String outflowCause;
    String causePoint;

    String originMeasures;
    String outflowMeasures;
    String defectType;
    
    // New fields
    String customer;
    Integer quantity;
    String conclusion;
    Long productId;
    
    // Images to upload and store in Attachment table
    List<MultipartFile> images;
}
