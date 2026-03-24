// ─── SendInAppNotificationRequest.java ───────────────────────────────────────
package com.sep490.anomaly_training_backend.dto.request;

import com.sep490.anomaly_training_backend.enums.InAppNotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendInAppNotificationRequest {

    @NotNull(message = "recipientId is required")
    Long recipientId;

    @NotBlank(message = "title is required")
    @Size(max = 150)
    String title;

    @NotBlank(message = "message is required")
    String message;

    @Builder.Default
    InAppNotificationType type = InAppNotificationType.INFO;

    String relatedEntityType;
    Long relatedEntityId;

    @Size(max = 500)
    String actionUrl;
}
