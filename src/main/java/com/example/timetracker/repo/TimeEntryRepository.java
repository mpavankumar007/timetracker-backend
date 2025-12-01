package com.example.timetracker.repo;

import com.example.timetracker.model.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {

    @Query("""
        SELECT t.employeeId AS employeeId,
               t.workDate   AS workDate,
               SUM(t.durationMs) AS totalMs
        FROM TimeEntry t
        WHERE t.workDate = :date
        GROUP BY t.employeeId, t.workDate
    """)
    List<DailyTotalProjection> findDailyTotals(@Param("date") LocalDate date);

    @Query("""
        SELECT t.employeeId AS employeeId,
               t.workDate   AS workDate,
               SUM(t.durationMs) AS totalMs
        FROM TimeEntry t
        WHERE t.workDate BETWEEN :startDate AND :endDate
        GROUP BY t.employeeId, t.workDate
        ORDER BY t.employeeId, t.workDate
    """)
    List<DailyTotalProjection> findTotalsBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate")   LocalDate endDate);

    interface DailyTotalProjection {
        String getEmployeeId();
        LocalDate getWorkDate();
        Long getTotalMs();
    }
}
