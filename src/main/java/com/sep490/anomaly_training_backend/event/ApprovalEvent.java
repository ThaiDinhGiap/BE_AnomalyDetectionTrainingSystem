package com.sep490.anomaly_training_backend.event;

import com.sep490.anomaly_training_backend.enums.ApprovalAction;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Domain event phát ra sau mỗi action trong approval workflow.
 * <p>
 * Được publish từ {@code ApprovalServiceImpl} và lắng nghe bởi
 * {@code ApprovalNotificationListener} (async).
 */
@Getter
public class ApprovalEvent extends ApplicationEvent {

    private final ApprovalAction action;
    private final ApprovalEntityType entityType;
    private final Long entityId;
    private final Long groupId;
    private final String entityLabel;
    private final User performedBy;
    private final ReportStatus newStatus;
    /** Username của người tạo entity (từ BaseEntity.createdBy) */
    private final String createdByUsername;

    public ApprovalEvent(Object source,
                         ApprovalAction action,
                         ApprovalEntityType entityType,
                         Long entityId,
                         Long groupId,
                         String entityLabel,
                         User performedBy,
                         ReportStatus newStatus,
                         String createdByUsername) {
        super(source);
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.groupId = groupId;
        this.entityLabel = entityLabel;
        this.performedBy = performedBy;
        this.newStatus = newStatus;
        this.createdByUsername = createdByUsername;
    }
}
