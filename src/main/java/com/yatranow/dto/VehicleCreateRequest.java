package com.yatranow.dto;

import com.yatranow.entity.Vehicle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record VehicleCreateRequest(
        @NotNull(message = "Vehicle type is required") Vehicle.VehicleType vehicleType,

        @NotBlank(message = "Vehicle number is required") String vehicleNumber,

        @NotBlank(message = "Vehicle name is required") String name,

        Vehicle.BusType busType, // Required only if vehicleType is BUS

        @Positive(message = "Total seats must be positive") Integer totalSeats // Optional, auto-calculated for buses
                                                                               // based on busType
) {
}
