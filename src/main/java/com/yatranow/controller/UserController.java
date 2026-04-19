package com.yatranow.controller;

import com.yatranow.dto.BookingRequest;
import com.yatranow.dto.BookingResponse;
import com.yatranow.dto.BogieResponse;
import com.yatranow.dto.ComplaintRequest;
import com.yatranow.dto.TrainSelectionRequest;
import com.yatranow.dto.TrainSelectionResponse;
import com.yatranow.entity.Complaint;
import com.yatranow.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ── Existing endpoints (unchanged) ────────────────────────────────────────

    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> bookTicket(
            @Valid @RequestBody BookingRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        BookingResponse response = userService.bookTicket(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getMyBookings(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<BookingResponse> bookings = userService.getMyBookings(userId);
        return ResponseEntity.ok(bookings);
    }

    @PostMapping(value = "/complaints", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Complaint> submitComplaint(
            @Valid @ModelAttribute ComplaintRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Complaint complaint = userService.submitComplaint(request, userId);
        return new ResponseEntity<>(complaint, HttpStatus.CREATED);
    }

    // ── Train Booking Flow endpoints ──────────────────────────────────────────

    /**
     * GET /api/user/train/bogies?vehicleId=&compartmentType=
     * Returns available bogies for the selected train vehicle and compartment type.
     * Requires USER role (JWT).
     *
     * @param vehicleId       the train vehicle's ID (from search result)
     * @param compartmentType one of: SECOND_SITTING, SLEEPER, AC
     */
    @GetMapping("/train/bogies")
    public ResponseEntity<List<BogieResponse>> getTrainBogies(
            @RequestParam Long vehicleId,
            @RequestParam String compartmentType,
            HttpServletRequest httpRequest) {
        // userId not required for listing bogies, but endpoint is still authenticated
        List<BogieResponse> bogies = userService.getBogiesByVehicleAndCompartment(vehicleId, compartmentType);
        return ResponseEntity.ok(bogies);
    }

    /**
     * POST /api/user/train/selection
     * Saves the user's chosen bogie/compartment for a train schedule.
     * Requires USER role (JWT).
     *
     * @param request TrainSelectionRequest with scheduleId, bogieId, compartmentType, bogieNumber
     */
    @PostMapping("/train/selection")
    public ResponseEntity<TrainSelectionResponse> saveTrainSelection(
            @Valid @RequestBody TrainSelectionRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        TrainSelectionResponse response = userService.saveTrainSelection(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
