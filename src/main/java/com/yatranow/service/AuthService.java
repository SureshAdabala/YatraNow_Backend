package com.yatranow.service;

import com.yatranow.dto.AuthResponse;
import com.yatranow.dto.LoginRequest;
import com.yatranow.dto.RegisterOwnerRequest;
import com.yatranow.dto.RegisterUserRequest;
import com.yatranow.entity.Admin;
import com.yatranow.entity.Owner;
import com.yatranow.entity.User;
import com.yatranow.exception.OwnerBlockedException;
import com.yatranow.repository.AdminRepository;
import com.yatranow.repository.OwnerRepository;
import com.yatranow.repository.UserRepository;
import com.yatranow.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ImageService imageService;
    private final OwnerService ownerService;

    @Transactional
    public AuthResponse registerUser(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadCredentialsException("Email already registered");
        }

        User user = new User();
        user.setName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setMobile(request.phoneNumber());
        user.setRole("USER");

        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());

        return new AuthResponse(token, user.getRole(), user.getName(), user.getEmail(), user.getId());
    }

    @Transactional
    public AuthResponse registerOwner(RegisterOwnerRequest request) {
        if (ownerRepository.existsByEmail(request.getEmail())) {
            throw new BadCredentialsException("Email already registered");
        }

        // Validate and process agency image
        byte[] agencyImageBytes = imageService.processImageOptional(request.getAgencyImage());

        Owner owner = new Owner();
        owner.setOwnerName(request.getOwnerName());
        owner.setAgencyName(request.getAgencyName());
        owner.setAgencyImage(agencyImageBytes);
        owner.setEmail(request.getEmail());
        owner.setPassword(passwordEncoder.encode(request.getPassword()));
        owner.setMobile(request.getMobile());
        owner.setRole("OWNER");
        owner.setIsBlocked(false);

        owner = ownerRepository.save(owner);

        // Create a default vehicle for the owner to prevent "Add Vehicle" redirect
        createDefaultVehicle(owner);

        String token = jwtUtil.generateToken(owner.getEmail(), owner.getRole(), owner.getId());

        return new AuthResponse(token, owner.getRole(), owner.getOwnerName(), owner.getEmail(), owner.getId());
    }

    public AuthResponse login(LoginRequest request) {
        // Try Admin first
        var adminOpt = adminRepository.findByEmail(request.email());
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (passwordEncoder.matches(request.password(), admin.getPassword())) {
                String token = jwtUtil.generateToken(admin.getEmail(), admin.getRole(), admin.getId());
                return new AuthResponse(token, admin.getRole(), admin.getName(), admin.getEmail(), admin.getId());
            }
        }

        // Try Owner second
        var ownerOpt = ownerRepository.findByEmail(request.email());
        if (ownerOpt.isPresent()) {
            Owner owner = ownerOpt.get();
            if (owner.getIsBlocked()) {
                throw new OwnerBlockedException("Your account has been blocked by admin");
            }
            if (passwordEncoder.matches(request.password(), owner.getPassword())) {
                String token = jwtUtil.generateToken(owner.getEmail(), owner.getRole(), owner.getId());
                return new AuthResponse(token, owner.getRole(), owner.getOwnerName(), owner.getEmail(), owner.getId());
            }
        }

        // Try User last
        var userOpt = userRepository.findByEmail(request.email());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getIsBlocked()) {
                throw new OwnerBlockedException("Your account has been blocked by admin");
            }
            if (passwordEncoder.matches(request.password(), user.getPassword())) {
                String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());
                return new AuthResponse(token, user.getRole(), user.getName(), user.getEmail(), user.getId());
            }
        }

        throw new BadCredentialsException("Invalid email or password");
    }

    private void createDefaultVehicle(Owner owner) {
        try {
            // Create a default bus
            com.yatranow.entity.Vehicle.BusType defaultBusType = com.yatranow.entity.Vehicle.BusType.SEATER;
            com.yatranow.dto.VehicleCreateRequest vehicleRequest = new com.yatranow.dto.VehicleCreateRequest(
                    com.yatranow.entity.Vehicle.VehicleType.BUS,
                    "DEF-" + owner.getMobile().substring(Math.max(0, owner.getMobile().length() - 4)) + "-"
                            + System.currentTimeMillis() % 1000, // Semi-unique number
                    "Default Bus",
                    defaultBusType,
                    defaultBusType.getSeatCount());
            ownerService.createVehicle(vehicleRequest, owner.getId());
        } catch (Exception e) {
            // Log but don't fail registration if vehicle creation fails
            System.err.println("Failed to create default vehicle for owner: " + e.getMessage());
        }
    }
}
