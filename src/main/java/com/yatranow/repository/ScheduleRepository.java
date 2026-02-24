package com.yatranow.repository;

import com.yatranow.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

        List<Schedule> findByVehicleId(Long vehicleId);

        List<Schedule> findByRouteId(Long routeId);

        List<Schedule> findByScheduleDate(LocalDate scheduleDate);

        @Query("""
                        SELECT s FROM Schedule s
                        JOIN s.route r
                        WHERE LOWER(r.fromLocation) = LOWER(:fromLocation)
                        AND LOWER(r.toLocation) = LOWER(:toLocation)
                        AND s.scheduleDate = :scheduleDate
                        AND s.availableSeats > 0
                        """)
        Page<Schedule> searchSchedules(
                        @Param("fromLocation") String fromLocation,
                        @Param("toLocation") String toLocation,
                        @Param("scheduleDate") LocalDate scheduleDate,
                        Pageable pageable);

        @Query("SELECT s FROM Schedule s WHERE s.vehicleId IN :vehicleIds")
        List<Schedule> findByVehicleIdIn(@Param("vehicleIds") List<Long> vehicleIds);

        // Fetch schedules for today or future
        List<Schedule> findByScheduleDateGreaterThanEqual(LocalDate date);

        // Find past schedules for cleanup
        List<Schedule> findByScheduleDateBefore(LocalDate date);

        // Delete past schedules
        void deleteByScheduleDateBefore(LocalDate date);

        void deleteByVehicleIdIn(List<Long> vehicleIds);
}
