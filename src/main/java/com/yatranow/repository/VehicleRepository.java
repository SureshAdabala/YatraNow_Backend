package com.yatranow.repository;

import com.yatranow.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByOwnerId(Long ownerId);

    List<Vehicle> findByVehicleType(Vehicle.VehicleType vehicleType);

    boolean existsByVehicleNumber(String vehicleNumber);

    void deleteByOwnerId(Long ownerId);
}
