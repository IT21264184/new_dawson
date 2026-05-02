package com.example.Dawson.Bungalow.service;


import com.example.Dawson.Bungalow.dto.BookingRequest;
import com.example.Dawson.Bungalow.dto.BookingResponse;
import com.example.Dawson.Bungalow.model.Booking;
import com.example.Dawson.Bungalow.model.Room;
import com.example.Dawson.Bungalow.model.User;
import com.example.Dawson.Bungalow.repository.BookingRepository;
import com.example.Dawson.Bungalow.repository.RoomRepository;
import com.example.Dawson.Bungalow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // CUSTOMER METHODS
    // ─────────────────────────────────────────────────────────────────────────

    public BookingResponse createBooking(String userId, BookingRequest request) {

        // 1. Validate dates
        validateDates(request.getCheckInDate(), request.getCheckOutDate());

        // 2. Load room
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // 3. Check guest capacity
        if (request.getGuests() > room.getCapacity()) {
            throw new RuntimeException(
                    "This room fits up to " + room.getCapacity() +
                            " guests. You requested " + request.getGuests() + ".");
        }

        // 4. Check availability — reject if any pending/confirmed booking overlaps
        List<?> overlaps = bookingRepository.findOverlappingBookings(
                request.getRoomId(),
                request.getCheckInDate(),
                request.getCheckOutDate());

        if (!overlaps.isEmpty()) {
            throw new RuntimeException("Room is not available for the selected dates.");
        }

        // 5. Build and save booking
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setRoomId(request.getRoomId());
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setGuests(request.getGuests());
        booking.setSpecialRequests(request.getSpecialRequests());
        booking.setStatus("pending");

        Booking saved = bookingRepository.save(booking);

        // 6. Enrich and return
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return BookingResponse.from(saved, room, user);
    }

    public List<BookingResponse> getMyBookings(String userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::enrich)
                .toList();
    }

    public BookingResponse getMyBookingById(String bookingId, String userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        return enrich(booking);
    }

    public BookingResponse cancelMyBooking(String bookingId, String userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        if (booking.getStatus().equals("cancelled")) {
            throw new RuntimeException("Booking is already cancelled.");
        }

        if (booking.getStatus().equals("confirmed")) {
            throw new RuntimeException(
                    "Confirmed bookings can only be cancelled by admin. Please contact us.");
        }

        booking.setStatus("cancelled");
        return enrich(bookingRepository.save(booking));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN METHODS
    // ─────────────────────────────────────────────────────────────────────────

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::enrich)
                .toList();
    }

    public List<BookingResponse> getBookingsByStatus(String status) {
        return bookingRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::enrich)
                .toList();
    }

    public BookingResponse getBookingById(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return enrich(booking);
    }

    public BookingResponse confirmBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getStatus().equals("pending")) {
            throw new RuntimeException("Only pending bookings can be confirmed.");
        }

        booking.setStatus("confirmed");
        return enrich(bookingRepository.save(booking));
    }

    public BookingResponse cancelBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus().equals("cancelled")) {
            throw new RuntimeException("Booking is already cancelled.");
        }

        booking.setStatus("cancelled");
        return enrich(bookingRepository.save(booking));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SHARED HELPER — used by BookingController to convert email → userId
    // ─────────────────────────────────────────────────────────────────────────

    public String resolveUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private BookingResponse enrich(Booking booking) {
        Room room = roomRepository.findById(booking.getRoomId())
                .orElseThrow(() -> new RuntimeException(
                        "Room not found for booking " + booking.getId()));

        User user = userRepository.findById(booking.getUserId())
                .orElseThrow(() -> new RuntimeException(
                        "User not found for booking " + booking.getId()));

        return BookingResponse.from(booking, room, user);
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new RuntimeException("Check-in and check-out dates are required.");
        }
        if (!checkIn.isAfter(LocalDate.now().minusDays(1))) {
            throw new RuntimeException("Check-in date must be today or in the future.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new RuntimeException("Check-out date must be after check-in date.");
        }
    }

}
