-- ============================================================
-- YatraNow — Seed data for train bogies
-- ============================================================
-- These INSERT IGNORE statements seed demo bogies for any train
-- vehicles already in the database. They are safe to run multiple
-- times (will skip duplicates due to IGNORE + unique constraint).
--
-- The vehicle_id values here are examples. If your train vehicles
-- have different IDs, update the vehicle_id column accordingly,
-- or add more rows following the same pattern.
--
-- CompartmentType values: SECOND_SITTING | SLEEPER | AC
-- ============================================================

-- ── Bogies for train vehicle ID = 1 ──
INSERT IGNORE INTO bogies (vehicle_id, bogie_number, compartment_type, total_seats, is_available)
VALUES
    (1, 'D1', 'SECOND_SITTING', 90, true),
    (1, 'D2', 'SECOND_SITTING', 90, true),
    (1, 'D3', 'SECOND_SITTING', 90, true),
    (1, 'D4', 'SECOND_SITTING', 90, true),
    (1, 'S1', 'SLEEPER',        72, true),
    (1, 'S2', 'SLEEPER',        72, true),
    (1, 'S3', 'SLEEPER',        72, true),
    (1, 'A1', 'AC',             64, true),
    (1, 'A2', 'AC',             64, true);

-- ── Bogies for train vehicle ID = 2 ──
INSERT IGNORE INTO bogies (vehicle_id, bogie_number, compartment_type, total_seats, is_available)
VALUES
    (2, 'D1', 'SECOND_SITTING', 90, true),
    (2, 'D2', 'SECOND_SITTING', 90, true),
    (2, 'S1', 'SLEEPER',        72, true),
    (2, 'S2', 'SLEEPER',        72, true),
    (2, 'A1', 'AC',             64, true);

-- ── Bogies for train vehicle ID = 3 ──
INSERT IGNORE INTO bogies (vehicle_id, bogie_number, compartment_type, total_seats, is_available)
VALUES
    (3, 'D1', 'SECOND_SITTING', 90, true),
    (3, 'D2', 'SECOND_SITTING', 90, true),
    (3, 'S1', 'SLEEPER',        72, true),
    (3, 'A1', 'AC',             64, true);
