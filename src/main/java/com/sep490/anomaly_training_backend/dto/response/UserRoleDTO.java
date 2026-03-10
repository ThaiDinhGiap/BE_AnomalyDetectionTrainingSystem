package com.sep490.anomaly_training_backend.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleDTO {
    private Long id;
    private String roleCode;
    private String displayName;
    private Boolean isActive;
}
