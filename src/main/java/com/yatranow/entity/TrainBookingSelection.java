package com.yatranow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Records the user's compartment + bogie selection for a train booking session.
 * Created when the user selects a bogie, before or alongside seat booking.
 */
@Entity
@Table(name = "train_booking_selections")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainBookingSelection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "bogie_id", nullable = false)
    private Long bogieId;

    @Column(name = "compartment_type", nullable = false, length = 20)
    private String compartmentType; // Stored as string for flexibility

    @Column(name = "bogie_number", nullable = false, length = 10)
    private String bogieNumber;

    @CreationTimestamp
    @Column(name = "selected_at", updatable = false)
    private LocalDateTime selectedAt;
}
