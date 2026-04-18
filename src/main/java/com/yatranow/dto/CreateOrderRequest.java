package com.yatranow.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request body for POST /api/payment/create-order
 * Frontend sends this after the user fills passenger details.
 */
public record CreateOrderRequest(

        @NotNull(message = "Schedule ID is required")
        Long scheduleId,

        @NotEmpty(message = "At least one seat must be selected")
        List<String> seatNumbers,

        /** Amount in paise (e.g. ₹499 → 49900 paise) */
        @Min(value = 100, message = "Minimum amount is 1 INR (100 paise)")
        long amountInPaise
) {}
