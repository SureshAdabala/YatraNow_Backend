package com.yatranow.controller;

import com.yatranow.dto.SearchRequest;
import com.yatranow.dto.SearchResponse;
import com.yatranow.entity.Complaint;
import com.yatranow.entity.Owner;
import com.yatranow.entity.Route;
import com.yatranow.exception.ResourceNotFoundException;
import com.yatranow.repository.ComplaintRepository;
import com.yatranow.repository.OwnerRepository;
import com.yatranow.repository.RouteRepository;
import com.yatranow.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final UserService userService;
    private final OwnerRepository ownerRepository;
    private final ComplaintRepository complaintRepository;
    private final RouteRepository routeRepository;

    @GetMapping("/routes")
    public ResponseEntity<List<SearchResponse>> getAllRoutes() {
        List<SearchResponse> routes = userService.getAllSchedules();
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/route-list")
    public ResponseEntity<List<Route>> getRouteList() {
        List<Route> routes = routeRepository.findAll();
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/seats/{scheduleId}")
    public ResponseEntity<List<String>> getBookedSeats(@PathVariable Long scheduleId) {
        List<String> bookedSeats = userService.getBookedSeatNumbers(scheduleId);
        return ResponseEntity.ok(bookedSeats);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<SearchResponse>> searchVehicles(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "price") String sortBy) {

        SearchRequest request = new SearchRequest(from.trim(), to.trim(), date);

        Sort sort = switch (sortBy) {
            case "price" -> Sort.by("price").ascending();
            case "departureTime" -> Sort.by("departureTime").ascending();
            default -> Sort.by("price").ascending();
        };

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SearchResponse> results = userService.searchVehicles(request, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/images/agency/{ownerId}")
    public ResponseEntity<byte[]> getAgencyImage(@PathVariable Long ownerId) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        if (owner.getAgencyImage() == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);

        return new ResponseEntity<>(owner.getAgencyImage(), headers, HttpStatus.OK);
    }

    @GetMapping("/images/complaint/{complaintId}")
    public ResponseEntity<byte[]> getComplaintImage(@PathVariable Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        if (complaint.getComplaintImage() == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);

        return new ResponseEntity<>(complaint.getComplaintImage(), headers, HttpStatus.OK);
    }
}
