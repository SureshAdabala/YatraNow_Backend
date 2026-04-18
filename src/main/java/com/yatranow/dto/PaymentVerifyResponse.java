package com.yatranow.dto;

import java.util.List;

/**
 * Response body for POST /api/payment/verify
 */
public record PaymentVerifyResponse(
        boolean success,
        String message,
        /** ID of the first created booking (used by frontend for QR / success page) */
        Long primaryBookingId,
        /** All booking IDs created in this transaction */
        List<Long> allBookingIds
) {}
