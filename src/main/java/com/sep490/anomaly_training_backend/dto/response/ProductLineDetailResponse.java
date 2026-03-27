package com.sep490.anomaly_training_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductLineDetailResponse {

    // Dây chuyền
    private Long lineId;
    private String lineCode;
    private String lineName;

    // Quản lý
    private Long groupId;
    private String groupName;
    private Long supervisorId;
    private String supervisorName;

    private Long sectionId;
    private String sectionName;
    private Long managerId;
    private String managerName;

    // Danh sách công đoạn
    private List<ProcessInfo> processes;

    // Danh sách sản phẩm + công đoạn
    private List<ProductWithProcesses> products;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessInfo {
        private Long processId;
        private String processCode;
        private String processName;
        private Integer classification;
        private BigDecimal standardTimeJt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductWithProcesses {
        private Long productId;
        private String productCode;
        private String productName;
        private String description;
        private List<String> attachmentUrls;
        private List<ProductProcessInfo> processes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductProcessInfo {
        private Long processId;
        private String processCode;
        private String processName;
        private BigDecimal standardTimeJt;
    }
}
