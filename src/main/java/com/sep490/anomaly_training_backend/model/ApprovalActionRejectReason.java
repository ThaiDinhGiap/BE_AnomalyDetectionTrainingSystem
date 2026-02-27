package com.sep490.anomaly_training_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * Entity for approval_action_reject_reasons table - N:M between approval_action and reject_reason
 */
@Entity
@Table(name = "approval_action_reject_reasons", uniqueConstraints = {
        @UniqueConstraint(name = "uk_aarr_unique", columnNames = {"approval_action_id", "reject_reason_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ApprovalActionRejectReason extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approval_action_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    ApprovalActionLog approvalAction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reject_reason_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    RejectReason rejectReason;
}
