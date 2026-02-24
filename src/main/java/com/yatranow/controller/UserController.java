package com.yatranow.controller;

import com.yatranow.dto.BookingRequest;
import com.yatranow.dto.BookingResponse;
import com.yatranow.dto.ComplaintRequest;
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
}
