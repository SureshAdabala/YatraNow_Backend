package com.yatranow.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleCreateRequest(
        @NotNull(message = "Vehicle ID is required") Long vehicleId,

        @NotNull(message = "Route ID is required") Long routeId,

        @NotNull(message = "Departure time is required") LocalTime departureTime,

        @NotNull(message = "Arrival time is required") LocalTime arrivalTime,

        @NotNull(message = "Price is required") @Positive(message = "Price must be positive") Double price,

        @NotNull(message = "Schedule date is required") LocalDate scheduleDate) {
}
