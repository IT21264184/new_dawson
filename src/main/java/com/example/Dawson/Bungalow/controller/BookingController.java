package com.example.Dawson.Bungalow.controller;

import com.example.Dawson.Bungalow.dto.BookingRequest;
import com.example.Dawson.Bungalow.dto.BookingResponse;
import com.example.Dawson.Bungalow.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;


    @PostMapping
    public ResponseEntity<?> createBooking(Authentication auth,
                                           @RequestBody BookingRequest request) {
        try {
            String userId = bookingService.resolveUserIdByEmail(auth.getName());
            BookingResponse response = bookingService.createBooking(userId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings(Authentication auth) {
        String userId = bookingService.resolveUserIdByEmail(auth.getName());
        return ResponseEntity.ok(bookingService.getMyBookings(userId));
    }


    @GetMapping("/my/{id}")
    public ResponseEntity<?> getMyBookingById(Authentication auth,
                                              @PathVariable String id) {
        try {
            String userId = bookingService.resolveUserIdByEmail(auth.getName());
            return ResponseEntity.ok(bookingService.getMyBookingById(id, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PatchMapping("/my/{id}/cancel")
    public ResponseEntity<?> cancelMyBooking(Authentication auth,
                                             @PathVariable String id) {
        try {
            String userId = bookingService.resolveUserIdByEmail(auth.getName());
            return ResponseEntity.ok(bookingService.cancelMyBooking(id, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }



    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getAllBookings(
            @RequestParam(required = false) String status) {

        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(bookingService.getBookingsByStatus(status));
        }
        return ResponseEntity.ok(bookingService.getAllBookings());
    }


    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getBookingById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(bookingService.getBookingById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PatchMapping("/admin/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> confirmBooking(@PathVariable String id) {
        try {
            return ResponseEntity.ok(bookingService.confirmBooking(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PatchMapping("/admin/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cancelBooking(@PathVariable String id) {
        try {
            return ResponseEntity.ok(bookingService.cancelBooking(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}