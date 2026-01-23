package com.sep490.anomaly_training_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;

@Entity
@Table(name = "notification_settings")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class NotificationSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_code", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    NotificationTemplate template;

    @Column(name = "is_enabled")
    @Builder.Default
    Boolean isEnabled = true;

    @Column(name = "remind_before_days")
    @Builder.Default
    Integer remindBeforeDays = 3;

    @Column(name = "is_persistent")
    @Builder.Default
    Boolean isPersistent = false; // true = gửi liên tục đến khi xử lý

    @Column(name = "remind_interval_hours")
    @Builder.Default
    Integer remindIntervalHours = 24;

    @Column(name = "max_reminders")
    @Builder.Default
    Integer maxReminders = 5;

    @Column(name = "preferred_send_time")
    @Builder.Default
    LocalTime preferredSendTime = LocalTime.of(8, 0);

    @Column(name = "escalate_after_days")
    Integer escalateAfterDays; // Sau bao nhiêu ngày thì escalate lên cấp cao hơn
}