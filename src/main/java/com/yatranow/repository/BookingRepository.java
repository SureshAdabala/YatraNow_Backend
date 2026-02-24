package com.yatranow.repository;

import com.yatranow.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findByScheduleId(Long scheduleId);

    boolean existsByScheduleIdAndSeatNumber(Long scheduleId, String seatNumber);

    @Query("""
            SELECT b FROM Booking b
            JOIN b.schedule s
            WHERE s.vehicleId IN :vehicleIds
            """)
    List<Booking> findByVehicleIds(@Param("vehicleIds") List<Long> vehicleIds);

    void deleteByScheduleIdIn(List<Long> scheduleIds);
}
