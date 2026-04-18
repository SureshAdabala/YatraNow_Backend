package com.yatranow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request body for POST /api/payment/verify
 * Sent by the frontend after Razorpay returns the payment success callback.
 */
public record PaymentVerifyRequest(

        @NotBlank(message = "Razorpay order ID is required")
        String razorpayOrderId,

        @NotBlank(message = "Razorpay payment ID is required")
        String razorpayPaymentId,

        @NotBlank(message = "Razorpay signature is required")
        String razorpaySignature,

        // ── Booking data (used to create bookings after payment is verified) ──

        @NotNull(message = "Schedule ID is required")
        Long scheduleId,

        @NotEmpty(message = "At least one passenger is required")
        List<@Valid PassengerDetail> passengers
) {

    /**
     * Details for each passenger / seat combination.
     */
    public record PassengerDetail(
            @NotBlank String seatNumber,
            @NotBlank String passengerName,
            @NotNull  Integer passengerAge,
            @NotBlank String passengerGender
    ) {}
}
