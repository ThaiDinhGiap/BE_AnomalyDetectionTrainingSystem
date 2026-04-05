package com.sep490.anomaly_training_backend.dto.response.sample;

import com.sep490.anomaly_training_backend.dto.response.ProductResponse;
import com.sep490.anomaly_training_backend.dto.response.defect.DefectResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TrainingSampleResponse {
    Long trainingSampleId;
    String trainingCode;
    Long productLineId;
    Long processId;
    String processName;
    ProductResponse product;
    DefectResponse defect;
    String trainingSampleCode;
    String categoryName;
    String trainingDescription;
    String note;
    Integer processOrder;
    Integer categoryOrder;
    Integer contentOrder;
    List<String> attachmentUrls;
}
