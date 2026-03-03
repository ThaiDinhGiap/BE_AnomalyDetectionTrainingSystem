package com.sep490.anomaly_training_backend.dto.response;

import lombok.*;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingSampleProposalUpdateResponse {
    Long id;
    Long productLineId;
    List<TrainingSampleProposalDetailUpdateResponse> detailUpdateResponses;
}
