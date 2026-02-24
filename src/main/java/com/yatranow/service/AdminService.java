package com.yatranow.service;

import com.yatranow.entity.Owner;
import com.yatranow.entity.User;
import com.yatranow.exception.ResourceNotFoundException;
import com.yatranow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final VehicleRepository vehicleRepository;
    private final ScheduleRepository scheduleRepository;
    private final BookingRepository bookingRepository;
    private final ComplaintRepository complaintRepository;
    private final SeatRepository seatRepository;

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<com.yatranow.entity.Booking> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable);
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    public Page<Owner> getAllOwners(Pageable pageable) {
        return ownerRepository.findAll(pageable);
    }

    @Transactional
    public Owner toggleBlockOwner(Long ownerId) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with ID: " + ownerId));

        owner.setIsBlocked(!owner.getIsBlocked());
        return ownerRepository.save(owner);
    }

    @Transactional
    public User toggleBlockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setIsBlocked(!user.getIsBlocked());
        return userRepository.save(user);
    }

    @Transactional
    public void deleteOwner(Long ownerId) {
        if (!ownerRepository.existsById(ownerId)) {
            throw new ResourceNotFoundException("Owner not found with ID: " + ownerId);
        }

        // 1. Fetch all vehicles for this owner
        List<com.yatranow.entity.Vehicle> vehicles = vehicleRepository.findByOwnerId(ownerId);
        List<Long> vehicleIds = vehicles.stream().map(com.yatranow.entity.Vehicle::getId).toList();

        if (!vehicleIds.isEmpty()) {
            // 2. Fetch all schedules for these vehicles
            List<com.yatranow.entity.Schedule> schedules = scheduleRepository.findByVehicleIdIn(vehicleIds);
            List<Long> scheduleIds = schedules.stream().map(com.yatranow.entity.Schedule::getId).toList();

            // 3. Delete Bookings (linked to schedules)
            if (!scheduleIds.isEmpty()) {
                bookingRepository.deleteByScheduleIdIn(scheduleIds);
            }

            // 4. Delete Schedules (linked to vehicles)
            scheduleRepository.deleteByVehicleIdIn(vehicleIds);

            // 5. Delete Complaints (linked to vehicles)
            complaintRepository.deleteByVehicleIdIn(vehicleIds);

            // 6. Delete Seats (linked to vehicles)
            seatRepository.deleteByVehicleIdIn(vehicleIds);

            // 7. Delete Vehicles
            vehicleRepository.deleteByOwnerId(ownerId);
        }

        // 8. Finally delete the owner
        ownerRepository.deleteById(ownerId);
    }
}
