package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.FactoryDayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.LocalDate;

@Entity
@Table(name = "factory_calendar_entries")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class FactoryCalendarEntry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "calendar_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    FactoryCalendar factoryCalendar;

    @Column(name = "work_date", nullable = false)
    LocalDate workDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false)
    FactoryDayType dayType;

    @Column(name = "holiday_name", length = 100)
    String holidayName;

    @Column(name = "holiday_code", length = 50)
    String holidayCode;

    @Column(name = "note")
    String note;

    /**
     * Lưu trữ dưới dạng JSON String.
     * Nếu dùng Hibernate 6+, có thể dùng @JdbcTypeCode(SqlTypes.JSON)
     */
    @Column(name = "tags_json", columnDefinition = "JSON")
    String tagsJson;
}