package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.FactoryCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FactoryCalendarRepository extends JpaRepository<FactoryCalendar, Long> {

    Optional<FactoryCalendar> findByCalendarYear(Integer calendarYear);

    List<FactoryCalendar> findByIsActiveTrueAndDeleteFlagFalse();
}
