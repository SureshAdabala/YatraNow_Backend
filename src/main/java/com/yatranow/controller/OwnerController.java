package com.yatranow.controller;

import com.yatranow.dto.RouteCreateRequest;
import com.yatranow.dto.ScheduleCreateRequest;
import com.yatranow.dto.VehicleCreateRequest;
import com.yatranow.entity.*;
import com.yatranow.service.OwnerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    @PostMapping("/vehicles")
    public ResponseEntity<Vehicle> createVehicle(
            @Valid @RequestBody VehicleCreateRequest request,
            HttpServletRequest httpRequest) {
        Long ownerId = (Long) httpRequest.getAttribute("userId");
        Vehicle vehicle = ownerService.createVehicle(request, ownerId);
        return new ResponseEntity<>(vehicle, HttpStatus.CREATED);
    }

    @GetMapping("/vehicles")
    public ResponseEntity<List<Vehicle>> getMyVehicles(HttpServletRequest httpRequest) {
        Long ownerId = (Long) httpRequest.getAttribute("userId");
        List<Vehicle> vehicles = ownerService.getMyVehicles(ownerId);
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping("/routes")
    public ResponseEntity<Route> createRoute(@Valid @RequestBody RouteCreateRequest request) {
        Route route = ownerService.createRoute(request);
        return new ResponseEntity<>(route, HttpStatus.CREATED);
    }

    @GetMapping("/routes")
    public ResponseEntity<List<Route>> getAllRoutes() {
        List<Route> routes = ownerService.getAllRoutes();
        return ResponseEntity.ok(routes);
    }

    @PutMapping("/routes/{id}")
    public ResponseEntity<Route> updateRoute(@PathVariable Long id,
            @Valid @RequestBody RouteCreateRequest request) {
        Route route = ownerService.updateRoute(id, request);
        return ResponseEntity.ok(route);
    }

    @DeleteMapping("/routes/{id}")
    public ResponseEntity<Map<String, String>> deleteRoute(@PathVariable Long id) {
        ownerService.deleteRoute(id);
        return ResponseEntity.ok(Map.of("message", "Route deleted successfully"));
    }

    @PostMapping("/schedules")
    public ResponseEntity<Schedule> createSchedule(
            @Valid @RequestBody ScheduleCreateRequest request,
            HttpServletRequest httpRequest) {
        Long ownerId = (Long) httpRequest.getAttribute("userId");
        Schedule schedule = ownerService.createSchedule(request, ownerId);
        return new ResponseEntity<>(schedule, HttpStatus.CREATED);

    }

    @GetMapping("/schedules")
    public ResponseEntity<List<Schedule>> getMySchedules(HttpServletRequest httpRequest) {
        Long ownerId = (Long) httpRequest.getAttribute("userId");
        List<Schedule> schedules = ownerService.getMySchedules(ownerId);
        return ResponseEntity.ok(schedules);
    }

    @DeleteMapping("/schedules/{id}")
    public ResponseEntity<Map<String, String>> deleteSchedule(
            @PathVariable Long id, HttpServletRequest httpRequest) {
        Long ownerId = (Long) httpRequest.getAttribute("userId");
        ownerService.deleteSchedule(id, ownerId);
        return ResponseEntity.ok(Map.of("message", "Schedule deleted successfully"));
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getMyBookings(HttpServletRequest httpRequest) {
        Long ownerId = (Long) httpRequest.getAttribute("userId");
        List<Booking> bookings = ownerService.getMyBookings(ownerId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/complaints")
    public ResponseEntity<List<Complaint>> getMyComplaints(HttpServletRequest httpRequest) {
        Long ownerId = (Long) httpRequest.getAttribute("userId");
        List<Complaint> complaints = ownerService.getMyComplaints(ownerId);
        return ResponseEntity.ok(complaints);
    }
}
