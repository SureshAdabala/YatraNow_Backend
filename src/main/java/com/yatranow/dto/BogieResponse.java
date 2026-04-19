package com.yatranow.dto;

/**
 * Response DTO representing a single bogie/coach available for selection.
 */
public record BogieResponse(
        Long bogieId,
        String bogieNumber,
        String compartmentType,
        Integer totalSeats) {
}
