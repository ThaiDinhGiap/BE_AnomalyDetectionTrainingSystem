package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.model.Attachment;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.model.ProductProcess;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String code;
    private String name;
    private List<String> attachmentUrl;
    private List<ProcessResponse>  processes;
}

