package com.yatranow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO sent by the frontend when the user selects a bogie.
 * Carries the train schedule ID, the chosen bogie, and compartment type.
 */
public record TrainSelectionRequest(

        @NotNull(message = "Schedule ID is required")
        Long scheduleId,

        @NotNull(message = "Bogie ID is required")
        Long bogieId,

        @NotBlank(message = "Compartment type is required")
        String compartmentType, // "SECOND_SITTING", "SLEEPER", or "AC"

        @NotBlank(message = "Bogie number is required")
        String bogieNumber      // e.g. "D1", "S2"
) {
}
