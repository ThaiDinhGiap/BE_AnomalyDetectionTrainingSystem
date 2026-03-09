package com.sep490.anomaly_training_backend.dto.request;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class TrainingSampleProposalRequest {
    Long id;
    Long productLineId;
    ReportStatus status;
    List<TrainingSampleProposalDetailRequest> listDetail;
}
