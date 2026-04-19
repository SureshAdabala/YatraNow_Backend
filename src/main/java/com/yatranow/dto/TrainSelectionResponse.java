package com.yatranow.dto;

/**
 * Response DTO returned after the user's bogie selection is persisted.
 */
public record TrainSelectionResponse(
        Long selectionId,
        Long scheduleId,
        Long bogieId,
        String bogieNumber,
        String compartmentType) {
}
