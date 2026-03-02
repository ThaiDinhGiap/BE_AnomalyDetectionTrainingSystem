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
public class RoleDetailResponse {
    private Long id;
    private String roleCode;
    private String displayName;
    private String description;
    private Boolean isSystem;
    private Boolean isActive;
    private int permissionCount;
    private int userCount;
    private List<PermissionResponse> permissions;
}
