package com.sep490.anomaly_training_backend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;

/**
 * Segment inside a day (half-day leave, split shift, move line/team within the day).
 */
@Entity
@Table(
        name = "work_schedule_segments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_wss_ws_seg", columnNames = {"work_schedule_id", "segment_no"})
        },
        indexes = {
                @Index(name = "idx_wss_ws", columnList = "work_schedule_id"),
                @Index(name = "idx_wss_team", columnList = "team_id"),
                @Index(name = "idx_wss_line", columnList = "product_line_id"),
                @Index(name = "idx_wss_segment_type", columnList = "segment_type"),
                @Index(name = "idx_wss_delete_flag", columnList = "delete_flag")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class WorkScheduleSegment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_schedule_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    WorkSchedule workSchedule;

    @Column(name = "segment_no", nullable = false)
    Integer segmentNo;

    @Column(name = "start_time", nullable = false)
    LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    LocalTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_line_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    ProductLine productLine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Team team;

    @Column(name = "segment_type", nullable = false, length = 30)
    String segmentType; // WORK/LEAVE/HOLIDAY/TRAINING/OTHER

    @Column(name = "role_code", length = 50)
    String roleCode;

    @Column(name = "shift_code", length = 20)
    String shiftCode;

    @Column(name = "planned_minutes")
    Integer plannedMinutes;

    @Column(name = "actual_minutes")
    Integer actualMinutes;

    @Column(name = "ot_minutes")
    Integer otMinutes;

    @Column(name = "leave_type", length = 50)
    String leaveType;

    @Column(name = "leave_is_paid")
    Boolean leaveIsPaid;

    @Column(name = "leave_minutes")
    Integer leaveMinutes;

    @Column(name = "leave_approval_status", length = 20)
    String leaveApprovalStatus;

    @Column(name = "leave_reason", length = 255)
    String leaveReason;

    @Column(name = "holiday_code", length = 50)
    String holidayCode;

    @Column(name = "holiday_name", length = 100)
    String holidayName;

    @Column(name = "process_ids_json", columnDefinition = "json")
    String processIdsJson;

    @Column(name = "workstation_id", length = 64)
    String workstationId;

    @Column(name = "note", length = 255)
    String note;

    @Column(name = "tags_json", columnDefinition = "json")
    String tagsJson;
}