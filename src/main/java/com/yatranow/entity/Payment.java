package com.yatranow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Stores every Razorpay payment record.
 * Status lifecycle: CREATED → PAID  (or FAILED on signature mismatch / user cancel)
 */
@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Razorpay order ID returned by create-order API (e.g. order_xxxxxxxxxx) */
    @Column(name = "razorpay_order_id", nullable = false, unique = true, length = 100)
    private String razorpayOrderId;

    /** Razorpay payment ID returned after successful payment (e.g. pay_xxxxxxxxxx) */
    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;

    /** HMAC-SHA256 signature for verification */
    @Column(name = "razorpay_signature", length = 512)
    private String razorpaySignature;

    /** Amount in INR (Rupees) */
    @Column(nullable = false)
    private Double amount;

    @Column(length = 10)
    private String currency = "INR";

    /** CREATED → PAID or FAILED */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.CREATED;

    /** Unique receipt ID sent to Razorpay (RECEIPT-<scheduleId>-<userId>-<timestamp>) */
    @Column(name = "receipt_id", nullable = false, length = 100)
    private String receiptId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    /** Comma-separated seat numbers, e.g. "3,5,7" */
    @Column(name = "seat_numbers", nullable = false, length = 255)
    private String seatNumbers;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum PaymentStatus {
        CREATED, PAID, FAILED
    }
}
