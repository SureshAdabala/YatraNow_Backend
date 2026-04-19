package com.yatranow.repository;

import com.yatranow.entity.Bogie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BogieRepository extends JpaRepository<Bogie, Long> {

    /**
     * Fetch all available bogies for a given vehicle and compartment type.
     * Used by the train booking flow to populate the bogie selection step.
     */
    List<Bogie> findByVehicleIdAndCompartmentTypeAndIsAvailableTrue(
            Long vehicleId, Bogie.CompartmentType compartmentType);

    /**
     * Fetch all bogies for a vehicle (any compartment type, any availability).
     */
    List<Bogie> findByVehicleId(Long vehicleId);
}
