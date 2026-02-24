package com.yatranow.repository;

import com.yatranow.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByVehicleId(Long vehicleId);

    Optional<Seat> findByVehicleIdAndSeatNumber(Long vehicleId, String seatNumber);

    boolean existsByVehicleIdAndSeatNumber(Long vehicleId, String seatNumber);

    void deleteByVehicleIdIn(List<Long> vehicleIds);
}
