package com.yatranow.service;

import com.yatranow.dto.*;
import com.yatranow.entity.*;
import com.yatranow.exception.DuplicateBookingException;
import com.yatranow.exception.ResourceNotFoundException;
import com.yatranow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ScheduleRepository scheduleRepository;
    private final BookingRepository bookingRepository;
    private final ComplaintRepository complaintRepository;
    private final ImageService imageService;
    private final VehicleRepository vehicleRepository;
    private final RouteRepository routeRepository;
    private final OwnerRepository ownerRepository;

    public Page<SearchResponse> searchVehicles(SearchRequest request, Pageable pageable) {
        Page<Schedule> schedules = scheduleRepository.searchSchedules(
                request.fromLocation(),
                request.toLocation(),
                request.date(),
                pageable);

        return schedules.map(this::convertToSearchResponse);
    }

    public List<SearchResponse> getAllSchedules() {
        // Only return schedules from today onwards
        List<Schedule> schedules = scheduleRepository.findByScheduleDateGreaterThanEqual(LocalDate.now());
        return schedules.stream()
                .map(this::convertToSearchResponse)
                .collect(Collectors.toList());
    }

    public List<String> getBookedSeatNumbers(Long scheduleId) {
        List<Booking> bookings = bookingRepository.findByScheduleId(scheduleId);
        return bookings.stream()
                .map(Booking::getSeatNumber)
                .collect(Collectors.toList());
    }

    private SearchResponse convertToSearchResponse(Schedule schedule) {
        Vehicle vehicle = vehicleRepository.findById(schedule.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        Route route = routeRepository.findById(schedule.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        Owner owner = ownerRepository.findById(vehicle.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        return new SearchResponse(
                schedule.getId(),
                vehicle.getId(),
                vehicle.getName(),
                vehicle.getVehicleNumber(),
                vehicle.getVehicleType().name(),
                vehicle.getBusType() != null ? vehicle.getBusType().name() : "N/A",
                route.getFromLocation(),
                route.getToLocation(),
                schedule.getScheduleDate(),
                schedule.getDepartureTime(),
                schedule.getArrivalTime(),
                schedule.getPrice(),
                schedule.getAvailableSeats(),
                owner.getId(),
                owner.getOwnerName(),
                owner.getAgencyName());
    }

    @Transactional
    public BookingResponse bookTicket(BookingRequest request, Long userId) {
        // Check if schedule exists
        Schedule schedule = scheduleRepository.findById(request.scheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        // Check for available seats
        if (schedule.getAvailableSeats() <= 0) {
            throw new IllegalStateException("No seats available for this schedule");
        }

        // Prevent double booking
        if (bookingRepository.existsByScheduleIdAndSeatNumber(request.scheduleId(), request.seatNumber())) {
            throw new DuplicateBookingException("This seat is already booked");
        }

        // Create booking
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setScheduleId(request.scheduleId());
        booking.setSeatNumber(request.seatNumber());
        booking.setPassengerName(request.passengerName());
        booking.setPassengerAge(request.passengerAge());
        booking.setPassengerGender(request.passengerGender());
        booking.setStatus(Booking.BookingStatus.CONFIRMED);

        booking = bookingRepository.save(booking);

        // Update available seats
        schedule.setAvailableSeats(schedule.getAvailableSeats() - 1);
        scheduleRepository.save(schedule);

        // Build response
        return buildBookingResponse(booking, schedule);
    }

    private BookingResponse buildBookingResponse(Booking booking, Schedule schedule) {
        Vehicle vehicle = vehicleRepository.findById(schedule.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        Route route = routeRepository.findById(schedule.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        return new BookingResponse(
                booking.getId(),
                schedule.getId(),
                booking.getSeatNumber(),
                booking.getPassengerName(),
                booking.getPassengerAge(),
                booking.getPassengerGender(),
                vehicle.getName(),
                vehicle.getVehicleNumber(),
                route.getFromLocation(),
                route.getToLocation(),
                schedule.getDepartureTime().toString(),
                schedule.getArrivalTime().toString(),
                schedule.getPrice(),
                booking.getBookingDate(),
                booking.getStatus().name());
    }

    public List<BookingResponse> getMyBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserId(userId);

        return bookings.stream()
                .map(booking -> {
                    Schedule schedule = scheduleRepository.findById(booking.getScheduleId())
                            .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));
                    return buildBookingResponse(booking, schedule);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Complaint submitComplaint(ComplaintRequest request, Long userId) {
        // Verify vehicle exists
        if (!vehicleRepository.existsById(request.getVehicleId())) {
            throw new ResourceNotFoundException("Vehicle not found");
        }

        // Process optional image
        byte[] complaintImageBytes = imageService.processImageOptional(request.getComplaintImage());

        Complaint complaint = new Complaint();
        complaint.setUserId(userId);
        complaint.setVehicleId(request.getVehicleId());
        complaint.setComplaintText(request.getComplaintText());
        complaint.setComplaintImage(complaintImageBytes);

        return complaintRepository.save(complaint);
    }
}
