package com.sep490.anomaly_training_backend.dto.request;

import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalDetailUpdateResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TrainingSampleProposalUpdateRequest {
    Long id;
    Long productLineId;
    List<TrainingSampleProposalDetailUpdateRequest> detailUpdateRequests;
}
