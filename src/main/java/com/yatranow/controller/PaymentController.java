package com.yatranow.controller;

import com.yatranow.dto.CreateOrderRequest;
import com.yatranow.dto.CreateOrderResponse;
import com.yatranow.dto.PaymentVerifyRequest;
import com.yatranow.dto.PaymentVerifyResponse;
import com.yatranow.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for Razorpay payment integration.
 * All endpoints require USER role (secured via SecurityConfig).
 *
 * POST /api/payment/create-order  — creates a Razorpay order
 * POST /api/payment/verify        — verifies signature & creates booking
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Step 1: Create a Razorpay order.
     * Frontend calls this when user clicks "Book Now" and submits passenger details.
     */
    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("Create order request from user {} for schedule {} seats {}",
                userId, request.scheduleId(), request.seatNumbers());

        CreateOrderResponse response = paymentService.createOrder(request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Step 2: Verify Razorpay signature and create bookings.
     * Frontend calls this after Razorpay Checkout fires the payment success handler.
     */
    @PostMapping("/verify")
    public ResponseEntity<PaymentVerifyResponse> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequest request,
            HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("Payment verify request from user {} for order {}",
                userId, request.razorpayOrderId());

        PaymentVerifyResponse response = paymentService.verifyAndBook(request, userId);
        return ResponseEntity.ok(response);
    }
}
