package com.sep490.anomaly_training_backend.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class DefectProposalUpdateRequest {
    Long id;
    Long productLineId;
    List<DefectProposalDetailUpdateRequest> listUpdatedItems;
}
