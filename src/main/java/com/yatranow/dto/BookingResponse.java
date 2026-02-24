package com.yatranow.dto;

import java.time.LocalDateTime;

public record BookingResponse(
        Long bookingId,
        Long scheduleId,
        String seatNumber,
        String passengerName,
        Integer passengerAge,
        String passengerGender,
        String vehicleName,
        String vehicleNumber,
        String fromLocation,
        String toLocation,
        String departureTime,
        String arrivalTime,
        Double price,
        LocalDateTime bookingDate,
        String status) {
}
