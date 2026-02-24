package com.yatranow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SearchRequest(
        @NotBlank(message = "From location is required") String fromLocation,

        @NotBlank(message = "To location is required") String toLocation,

        @NotNull(message = "Travel date is required") LocalDate date) {
}
