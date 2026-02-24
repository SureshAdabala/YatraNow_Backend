package com.yatranow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RouteCreateRequest(
        @NotBlank(message = "From location is required") String fromLocation,

        @NotBlank(message = "To location is required") String toLocation,

        @NotNull(message = "Distance is required") @Positive(message = "Distance must be positive") Double distanceKm) {
}
