package com.yatranow.dto;

/**
 * Response body for POST /api/payment/create-order
 * The frontend uses orderId + keyId to initialise the Razorpay Checkout widget.
 * NOTE: keyId is safe to expose; the secret stays on the server.
 */
public record CreateOrderResponse(
        String orderId,
        long amount,
        String currency,
        String keyId
) {}
