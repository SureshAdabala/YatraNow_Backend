package com.yatranow.controller;

import com.yatranow.entity.Booking;
import com.yatranow.entity.Owner;
import com.yatranow.entity.User;
import com.yatranow.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/bookings")
    public ResponseEntity<Page<Booking>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookings = adminService.getAllBookings(pageable);
        return ResponseEntity.ok(bookings);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @PatchMapping("/users/{id}/block")
    public ResponseEntity<Map<String, Object>> toggleBlockUser(@PathVariable Long id) {
        User user = adminService.toggleBlockUser(id);
        return ResponseEntity.ok(Map.of(
                "message", user.getIsBlocked() ? "User blocked successfully" : "User unblocked successfully",
                "isBlocked", user.getIsBlocked()));
    }

    @GetMapping("/owners")
    public ResponseEntity<Page<Owner>> getAllOwners(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Owner> owners = adminService.getAllOwners(pageable);
        return ResponseEntity.ok(owners);
    }

    @PatchMapping("/owners/{id}/block")
    public ResponseEntity<Map<String, Object>> toggleBlockOwner(@PathVariable Long id) {
        Owner owner = adminService.toggleBlockOwner(id);
        return ResponseEntity.ok(Map.of(
                "message", owner.getIsBlocked() ? "Owner blocked successfully" : "Owner unblocked successfully",
                "isBlocked", owner.getIsBlocked()));
    }

    @DeleteMapping("/owners/{id}")
    public ResponseEntity<Map<String, String>> deleteOwner(@PathVariable Long id) {
        adminService.deleteOwner(id);
        return ResponseEntity.ok(Map.of("message", "Owner deleted successfully"));
    }

}
