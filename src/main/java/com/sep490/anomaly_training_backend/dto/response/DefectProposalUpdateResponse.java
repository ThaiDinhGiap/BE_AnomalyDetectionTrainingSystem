package com.sep490.anomaly_training_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectProposalUpdateResponse {
    Long id;
    Long productLineId;
    List<DefectProposalDetailUpdateResponse> defectProposalDetail;
}
