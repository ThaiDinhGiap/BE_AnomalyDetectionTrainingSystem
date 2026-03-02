package com.sep490.anomaly_training_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleRequest {

    @NotNull(message = "Role IDs must not be null")
    private Set<Long> roleIds;
}
