package com.yatranow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a bogie/coach belonging to a train vehicle.
 * Each bogie has a compartment type (2S, Sleeper, AC) and belongs to a specific vehicle.
 */
@Entity
@Table(name = "bogies",
        uniqueConstraints = @UniqueConstraint(columnNames = {"vehicle_id", "bogie_number", "compartment_type"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bogie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "bogie_number", nullable = false, length = 10)
    private String bogieNumber; // e.g. "D1", "D2", "S1", "A1"

    @Enumerated(EnumType.STRING)
    @Column(name = "compartment_type", nullable = false, length = 20)
    private CompartmentType compartmentType;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    public enum CompartmentType {
        SECOND_SITTING, // 2S
        SLEEPER,        // Sleeper
        AC              // Air-Conditioned
    }
}
