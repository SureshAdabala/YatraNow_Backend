package com.yatranow.dto;

import jakarta.validation.constraints.*;

public record BookingRequest(
        @NotNull(message = "Schedule ID is required") Long scheduleId,

        @NotBlank(message = "Seat number is required") String seatNumber,

        @NotBlank(message = "Passenger name is required") @Size(min = 2, max = 100, message = "Passenger name must be between 2 and 100 characters") String passengerName,

        @NotNull(message = "Passenger age is required") @Min(value = 1, message = "Passenger age must be at least 1") @Max(value = 120, message = "Passenger age must be at most 120") Integer passengerAge,

        @NotBlank(message = "Passenger gender is required") @Pattern(regexp = "^(Male|Female|Other)$", message = "Gender must be Male, Female, or Other") String passengerGender) {
}
