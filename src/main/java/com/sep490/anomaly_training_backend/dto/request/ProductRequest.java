package com.sep490.anomaly_training_backend.dto.request;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
@Builder
public class ProductRequest {
    private Long id;
    private String code;
    private String name;
    private String description;
    private List<MultipartFile> images;
    private List<ProcessRequest> processes;
}
