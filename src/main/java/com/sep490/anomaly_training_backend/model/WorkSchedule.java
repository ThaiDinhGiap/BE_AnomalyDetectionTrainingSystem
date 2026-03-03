package com.sep490.anomaly_training_backend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Normalized daily work schedule header.
 * Upsert key in DB: UNIQUE(employee_id, work_date)
 */
@Entity
@Table(
        name = "work_schedules",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ws_emp_date", columnNames = {"employee_id", "work_date"})
        },
        indexes = {
                @Index(name = "idx_ws_line_date", columnList = "product_line_id,work_date"),
                @Index(name = "idx_ws_team_date", columnList = "team_id,work_date"),
                @Index(name = "idx_ws_employee_date", columnList = "employee_id,work_date"),
                @Index(name = "idx_ws_external_emp_date", columnList = "source_system,external_employee_id,work_date"),
                @Index(name = "idx_ws_day_type", columnList = "day_type"),
                @Index(name = "idx_ws_shift_code", columnList = "shift_code"),
                @Index(name = "idx_ws_delete_flag", columnList = "delete_flag")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class WorkSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "work_date", nullable = false)
    LocalDate workDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_line_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    ProductLine productLine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Employee employee;

    @Column(name = "source_system", nullable = false, length = 64)
    String sourceSystem;

    @Column(name = "external_employee_id", nullable = false, length = 64)
    String externalEmployeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sync_batch_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    ExternalScheduleSyncBatch syncBatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_record_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    ExternalScheduleRawRecord rawRecord;

    @Column(name = "role_code", length = 50)
    String roleCode;

    @Column(name = "day_type", nullable = false, length = 50)
    String dayType;

    @Column(name = "attendance_status", nullable = false, length = 50)
    String attendanceStatus;

    @Column(name = "shift_code", nullable = false, length = 20)
    String shiftCode;

    @Column(name = "shift_start_time")
    LocalTime shiftStartTime;

    @Column(name = "shift_end_time")
    LocalTime shiftEndTime;

    @Column(name = "break_minutes", nullable = false)
    @Builder.Default
    Integer breakMinutes = 0;

    @Column(name = "is_overnight", nullable = false)
    @Builder.Default
    Boolean isOvernight = false;

    @Column(name = "planned_minutes", nullable = false)
    @Builder.Default
    Integer plannedMinutes = 0;

    @Column(name = "actual_minutes")
    Integer actualMinutes;

    @Column(name = "ot_minutes_planned", nullable = false)
    @Builder.Default
    Integer otMinutesPlanned = 0;

    @Column(name = "ot_minutes_approved", nullable = false)
    @Builder.Default
    Integer otMinutesApproved = 0;

    @Column(name = "late_minutes", nullable = false)
    @Builder.Default
    Integer lateMinutes = 0;

    @Column(name = "early_leave_minutes", nullable = false)
    @Builder.Default
    Integer earlyLeaveMinutes = 0;

    @Column(name = "holiday_code", length = 50)
    String holidayCode;

    @Column(name = "holiday_name", length = 100)
    String holidayName;

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

    @Column(name = "note", length = 255)
    String note;

    @Column(name = "tags_json", columnDefinition = "json")
    String tagsJson;
}