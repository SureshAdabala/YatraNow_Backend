package com.yatranow.repository;

import com.yatranow.entity.TrainBookingSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainBookingSelectionRepository extends JpaRepository<TrainBookingSelection, Long> {

    /**
     * Retrieve all bogie selections made by a specific user.
     */
    List<TrainBookingSelection> findByUserId(Long userId);

    /**
     * Retrieve all bogie selections for a given schedule.
     */
    List<TrainBookingSelection> findByScheduleId(Long scheduleId);
}
