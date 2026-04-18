package com.yatranow.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.yatranow.dto.*;
import com.yatranow.entity.Booking;
import com.yatranow.entity.Payment;
import com.yatranow.entity.User;
import com.yatranow.exception.ResourceNotFoundException;
import com.yatranow.repository.BookingRepository;
import com.yatranow.repository.PaymentRepository;
import com.yatranow.repository.ScheduleRepository;
import com.yatranow.repository.UserRepository;
import com.yatranow.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Handles the complete Razorpay payment lifecycle:
 *  1. createOrder()  →  calls Razorpay API, saves Payment record (CREATED)
 *  2. verifyAndBook() → verifies HMAC signature, saves bookings, marks Payment (PAID)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final ScheduleRepository scheduleRepository;
    private final VehicleRepository vehicleRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    // ─────────────────────────────────────────────────────────────────────────
    // 1. CREATE ORDER
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a Razorpay order and persists a Payment record with status=CREATED.
     *
     * @param request   order creation request from frontend
     * @param userId    authenticated user's ID
     * @return          order details including Razorpay Key ID (safe for frontend)
     */
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request, Long userId) {
        // Validate schedule exists
        scheduleRepository.findById(request.scheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        // Build unique receipt ID
        String receiptId = "RECEIPT-" + request.scheduleId() + "-" + userId + "-" + System.currentTimeMillis();

        try {
            // ── Call Razorpay API ──
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", request.amountInPaise());   // in paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", receiptId);
            orderRequest.put("payment_capture", 1);                // auto-capture

            Order razorpayOrder = client.orders.create(orderRequest);
            String orderId = razorpayOrder.get("id");

            log.info("Razorpay order created: {} for user {} amount {} paise",
                    orderId, userId, request.amountInPaise());

            // ── Save Payment record (status = CREATED) ──
            Payment payment = new Payment();
            payment.setRazorpayOrderId(orderId);
            payment.setAmount(request.amountInPaise() / 100.0); // Store in Rupees in DB
            payment.setCurrency("INR");
            payment.setStatus(Payment.PaymentStatus.CREATED);
            payment.setReceiptId(receiptId);
            payment.setUserId(userId);
            payment.setScheduleId(request.scheduleId());
            payment.setSeatNumbers(String.join(",", request.seatNumbers()));
            paymentRepository.save(payment);

            return new CreateOrderResponse(orderId, request.amountInPaise(), "INR", razorpayKeyId);

        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new RuntimeException("Failed to create payment order. Please try again.", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. VERIFY PAYMENT AND CREATE BOOKINGS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Verifies the Razorpay signature using HMAC-SHA256.
     * On success: marks Payment as PAID and creates Booking records.
     * On failure: marks Payment as FAILED and throws an exception.
     *
     * @param request   contains the three Razorpay identifiers + booking passenger data
     * @param userId    authenticated user's ID
     * @return          booking IDs and success status
     */
    @Transactional
    public PaymentVerifyResponse verifyAndBook(PaymentVerifyRequest request, Long userId) {

        // ── 1. Find the stored Payment record ──
        Payment payment = paymentRepository.findByRazorpayOrderId(request.razorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment order not found. Please start the payment process again."));

        // ── 2. Verify HMAC-SHA256 signature ──
        boolean signatureValid = verifySignature(
                request.razorpayOrderId(),
                request.razorpayPaymentId(),
                request.razorpaySignature()
        );

        if (!signatureValid) {
            // Mark payment as FAILED
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setRazorpayPaymentId(request.razorpayPaymentId());
            paymentRepository.save(payment);

            log.warn("Invalid Razorpay signature for order: {}", request.razorpayOrderId());
            throw new SecurityException("Payment verification failed. Invalid signature.");
        }

        // ── 3. Create bookings for each passenger ──
        List<Long> bookingIds = new ArrayList<>();
        List<BookingResponse> bookingResponses = new ArrayList<>();

        for (PaymentVerifyRequest.PassengerDetail passenger : request.passengers()) {
            BookingRequest bookingRequest = new BookingRequest(
                    request.scheduleId(),
                    passenger.seatNumber(),
                    passenger.passengerName(),
                    passenger.passengerAge(),
                    passenger.passengerGender()
            );

            try {
                BookingResponse response = userService.bookTicket(bookingRequest, userId);
                bookingIds.add(response.bookingId());
                bookingResponses.add(response);
                log.info("Booking created: {} for seat {} user {}", response.bookingId(), passenger.seatNumber(), userId);
            } catch (Exception e) {
                log.error("Error creating booking for seat {}: {}", passenger.seatNumber(), e.getMessage());
                // Re-throw; @Transactional will rollback all bookings made so far
                throw new RuntimeException("Booking failed for seat " + passenger.seatNumber() + ": " + e.getMessage(), e);
            }
        }

        // ── 4. Mark payment as PAID ──
        payment.setStatus(Payment.PaymentStatus.PAID);
        payment.setRazorpayPaymentId(request.razorpayPaymentId());
        payment.setRazorpaySignature(request.razorpaySignature());
        paymentRepository.save(payment);

        log.info("Payment {} verified and {} booking(s) created for user {}",
                request.razorpayPaymentId(), bookingIds.size(), userId);
                
        // ── 5. Trigger Email Asynchronously ──
        if (!bookingResponses.isEmpty()) {
            CompletableFuture.runAsync(() -> {
                try {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found for email"));
                    emailService.sendBookingConfirmation(user, request.razorpayOrderId(), bookingResponses);
                } catch (Exception e) {
                    log.error("Fail to run email task asynchronously for user {}: {}", userId, e.getMessage());
                }
            });
        }

        return new PaymentVerifyResponse(
                true,
                "Payment verified and booking confirmed!",
                bookingIds.isEmpty() ? null : bookingIds.get(0),
                bookingIds
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER: HMAC-SHA256 Signature Verification
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Verifies the Razorpay signature.
     * Expected signature = HMAC_SHA256(orderId + "|" + paymentId, secretKey)
     */
    private boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);

            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generatedSignature = HexFormat.of().formatHex(hash);

            return generatedSignature.equals(signature);

        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }
}
