package com.yatranow.service;

import com.yatranow.dto.RouteCreateRequest;
import com.yatranow.dto.ScheduleCreateRequest;
import com.yatranow.dto.VehicleCreateRequest;
import com.yatranow.entity.*;
import com.yatranow.exception.ResourceNotFoundException;
import com.yatranow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final VehicleRepository vehicleRepository;
    private final SeatRepository seatRepository;
    private final RouteRepository routeRepository;
    private final ScheduleRepository scheduleRepository;
    private final BookingRepository bookingRepository;
    private final ComplaintRepository complaintRepository;

    @Transactional
    public Vehicle createVehicle(VehicleCreateRequest request, Long ownerId) {
        // Validate vehicle number uniqueness
        if (vehicleRepository.existsByVehicleNumber(request.vehicleNumber())) {
            throw new IllegalArgumentException("Vehicle number already exists");
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setOwnerId(ownerId);
        vehicle.setVehicleType(request.vehicleType());
        vehicle.setVehicleNumber(request.vehicleNumber());
        vehicle.setName(request.name());

        // Handle bus-specific logic
        if (request.vehicleType() == Vehicle.VehicleType.BUS) {
            if (request.busType() == null) {
                throw new IllegalArgumentException("Bus type is required for BUS vehicles");
            }
            vehicle.setBusType(request.busType());
            vehicle.setTotalSeats(request.busType().getSeatCount());
        } else {
            // For trains, use provided total seats or default
            vehicle.setTotalSeats(request.totalSeats() != null ? request.totalSeats() : 100);
        }

        vehicle = vehicleRepository.save(vehicle);

        // Auto-generate seats for buses
        if (request.vehicleType() == Vehicle.VehicleType.BUS) {
            generateSeatsForBus(vehicle);
        } else {
            // For trains, generate basic seats
            generateSeatsForTrain(vehicle);
        }

        return vehicle;
    }

    private void generateSeatsForBus(Vehicle vehicle) {
        List<Seat> seats = new ArrayList<>();

        switch (vehicle.getBusType()) {
            case SUPER_LUXURY:
                // 40 seats, 2x2 layout: 1A-10D
                for (int row = 1; row <= 10; row++) {
                    for (char col : new char[] { 'A', 'B', 'C', 'D' }) {
                        Seat seat = new Seat();
                        seat.setVehicleId(vehicle.getId());
                        seat.setSeatNumber(row + String.valueOf(col));
                        seat.setSeatType(Seat.SeatType.SEATER);
                        seat.setIsAvailable(true);
                        seats.add(seat);
                    }
                }
                break;

            case DELUXE:
                // 45 seats, mixed layout
                for (int row = 1; row <= 11; row++) {
                    for (char col : new char[] { 'A', 'B', 'C', 'D' }) {
                        Seat seat = new Seat();
                        seat.setVehicleId(vehicle.getId());
                        seat.setSeatNumber(row + String.valueOf(col));
                        seat.setSeatType(Seat.SeatType.SEATER);
                        seat.setIsAvailable(true);
                        seats.add(seat);
                        if (seats.size() >= 45)
                            break;
                    }
                    if (seats.size() >= 45)
                        break;
                }
                break;

            case SLEEPER:
                // 36 seats, 2x1 sleeper layout: L1-L18, U1-U18
                for (int i = 1; i <= 18; i++) {
                    Seat lowerSeat = new Seat();
                    lowerSeat.setVehicleId(vehicle.getId());
                    lowerSeat.setSeatNumber("L" + i);
                    lowerSeat.setSeatType(Seat.SeatType.SLEEPER);
                    lowerSeat.setIsAvailable(true);
                    seats.add(lowerSeat);

                    Seat upperSeat = new Seat();
                    upperSeat.setVehicleId(vehicle.getId());
                    upperSeat.setSeatNumber("U" + i);
                    upperSeat.setSeatType(Seat.SeatType.SLEEPER);
                    upperSeat.setIsAvailable(true);
                    seats.add(upperSeat);
                }
                break;

            case SEATER:
                // 52 seats, 3x2 layout: 1A-11E
                for (int row = 1; row <= 11; row++) {
                    for (char col : new char[] { 'A', 'B', 'C', 'D', 'E' }) {
                        Seat seat = new Seat();
                        seat.setVehicleId(vehicle.getId());
                        seat.setSeatNumber(row + String.valueOf(col));
                        seat.setSeatType(Seat.SeatType.SEATER);
                        seat.setIsAvailable(true);
                        seats.add(seat);
                        if (seats.size() >= 52)
                            break;
                    }
                    if (seats.size() >= 52)
                        break;
                }
                break;
        }

        seatRepository.saveAll(seats);
    }

    private void generateSeatsForTrain(Vehicle vehicle) {
        List<Seat> seats = new ArrayList<>();
        int totalSeats = vehicle.getTotalSeats();

        // Simple seat generation for trains (e.g., S1-S100)
        for (int i = 1; i <= totalSeats; i++) {
            Seat seat = new Seat();
            seat.setVehicleId(vehicle.getId());
            seat.setSeatNumber("S" + i);
            seat.setSeatType(Seat.SeatType.SEATER);
            seat.setIsAvailable(true);
            seats.add(seat);
        }

        seatRepository.saveAll(seats);
    }

    public List<Vehicle> getMyVehicles(Long ownerId) {
        return vehicleRepository.findByOwnerId(ownerId);
    }

    @Transactional
    public Route createRoute(RouteCreateRequest request) {
        Route route = new Route();
        route.setFromLocation(request.fromLocation());
        route.setToLocation(request.toLocation());
        route.setDistanceKm(request.distanceKm());
        return routeRepository.save(route);
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    @Transactional
    public Route updateRoute(Long id, RouteCreateRequest request) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));
        route.setFromLocation(request.fromLocation());
        route.setToLocation(request.toLocation());
        route.setDistanceKm(request.distanceKm());
        return routeRepository.save(route);
    }

    @Transactional
    public void deleteRoute(Long id) {
        if (!routeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Route not found");
        }

        // 1. Find all dependent schedules
        List<Schedule> dependentSchedules = scheduleRepository.findByRouteId(id);

        if (!dependentSchedules.isEmpty()) {
            List<Long> scheduleIds = dependentSchedules.stream()
                    .map(Schedule::getId)
                    .toList();

            // 2. Delete all bookings for these schedules
            bookingRepository.deleteByScheduleIdIn(scheduleIds);

            // 3. Delete the schedules
            scheduleRepository.deleteAll(dependentSchedules);
        }

        // 4. Finally delete the route
        routeRepository.deleteById(id);
    }

    @Transactional
    public Schedule createSchedule(ScheduleCreateRequest request, Long ownerId) {
        // Verify vehicle belongs to owner
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        if (!vehicle.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("You can only create schedules for your own vehicles");
        }

        // Verify route exists
        if (!routeRepository.existsById(request.routeId())) {
            throw new ResourceNotFoundException("Route not found");
        }

        Schedule schedule = new Schedule();
        schedule.setVehicleId(request.vehicleId());
        schedule.setRouteId(request.routeId());
        schedule.setDepartureTime(request.departureTime());
        schedule.setArrivalTime(request.arrivalTime());
        schedule.setPrice(request.price());
        schedule.setScheduleDate(request.scheduleDate());
        schedule.setAvailableSeats(vehicle.getTotalSeats());

        return scheduleRepository.save(schedule);
    }

    public List<com.yatranow.dto.BookingResponse> getMyBookings(Long ownerId) {
        List<Long> vehicleIds = vehicleRepository.findByOwnerId(ownerId)
                .stream()
                .map(Vehicle::getId)
                .toList();

        List<Booking> bookings = bookingRepository.findByVehicleIds(vehicleIds);

        return bookings.stream().map(b -> {
            Schedule schedule = b.getSchedule();
            Route route = schedule != null
                    ? routeRepository.findById(schedule.getRouteId()).orElse(null)
                    : null;

            String fromLocation = route != null ? route.getFromLocation() : "-";
            String toLocation = route != null ? route.getToLocation() : "-";
            Double price = schedule != null ? schedule.getPrice() : null;
            String departure = schedule != null && schedule.getDepartureTime() != null
                    ? schedule.getDepartureTime().toString()
                    : null;
            String arrival = schedule != null && schedule.getArrivalTime() != null
                    ? schedule.getArrivalTime().toString()
                    : null;
            String vehicleName = null;
            String vehicleNumber = null;
            if (schedule != null) {
                var vehicle = vehicleRepository.findById(schedule.getVehicleId()).orElse(null);
                if (vehicle != null) {
                    vehicleName = vehicle.getName();
                    vehicleNumber = vehicle.getVehicleNumber();
                }
            }

            return new com.yatranow.dto.BookingResponse(
                    b.getId(),
                    b.getScheduleId(),
                    b.getSeatNumber(),
                    b.getPassengerName(),
                    b.getPassengerAge(),
                    b.getPassengerGender(),
                    vehicleName,
                    vehicleNumber,
                    fromLocation,
                    toLocation,
                    departure,
                    arrival,
                    price,
                    b.getBookingDate(),
                    b.getStatus() != null ? b.getStatus().name() : "CONFIRMED");
        }).toList();
    }

    public List<Complaint> getMyComplaints(Long ownerId) {
        List<Long> vehicleIds = vehicleRepository.findByOwnerId(ownerId)
                .stream()
                .map(Vehicle::getId)
                .toList();

        return complaintRepository.findByVehicleIdIn(vehicleIds);
    }

    public List<Schedule> getMySchedules(Long ownerId) {
        List<Long> vehicleIds = vehicleRepository.findByOwnerId(ownerId)
                .stream()
                .map(Vehicle::getId)
                .toList();

        return scheduleRepository.findByVehicleIdIn(vehicleIds);
    }

    @Transactional
    public void deleteSchedule(Long scheduleId, Long ownerId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        Vehicle vehicle = vehicleRepository.findById(schedule.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        if (!vehicle.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("You can only delete your own schedules");
        }

        // Delete associated bookings first
        bookingRepository.deleteByScheduleIdIn(List.of(scheduleId));

        scheduleRepository.delete(schedule);
    }
}
