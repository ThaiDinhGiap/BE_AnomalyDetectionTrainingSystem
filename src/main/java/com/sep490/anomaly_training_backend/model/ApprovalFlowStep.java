package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "approval_flow_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalFlowStep extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private ApprovalEntityType entityType;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "required_permission", nullable = false, length = 100)
    private String requiredPermission;

    @Column(name = "step_label", length = 100)
    private String stepLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "pending_status", nullable = false, length = 50)
    private ReportStatus pendingStatus;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}