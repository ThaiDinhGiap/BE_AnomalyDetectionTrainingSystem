package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.FactoryDayType;
import com.sep490.anomaly_training_backend.model.FactoryCalendarEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FactoryCalendarEntryRepository extends JpaRepository<FactoryCalendarEntry, Long> {

    List<FactoryCalendarEntry> findByFactoryCalendarIdAndWorkDateBetweenOrderByWorkDate(
            Long calendarId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<FactoryCalendarEntry> findByFactoryCalendarIdAndDayType(Long calendarId, FactoryDayType dayType);
}
