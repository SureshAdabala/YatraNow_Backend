package com.yatranow.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record SearchResponse(
        Long scheduleId,
        Long vehicleId,
        String vehicleName,
        String vehicleNumber,
        String vehicleType,
        String busType,
        String fromLocation,
        String toLocation,
        LocalDate scheduleDate,
        LocalTime departureTime,
        LocalTime arrivalTime,
        Double price,
        Integer availableSeats,
        Long ownerId,
        String ownerName,
        String agencyName) {
}
