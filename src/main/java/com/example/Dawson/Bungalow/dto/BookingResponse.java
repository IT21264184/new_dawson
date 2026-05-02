package com.example.Dawson.Bungalow.dto;

import com.example.Dawson.Bungalow.model.Booking;
import com.example.Dawson.Bungalow.model.Room;
import com.example.Dawson.Bungalow.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

// Returned to the frontend — combines Booking data with
// enriched Room details and User name so the UI doesn't
// need to make extra API calls.

public class BookingResponse {

    private String id;
    private String status;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int guests;
    private String specialRequests;
    private long totalNights;
    private double totalPrice;
    private LocalDateTime createdAt;

    // Embedded room summary
    private String roomId;
    private String roomNumber;
    private String roomType;
    private double pricePerNight;
    private List<String> roomImages;

    // Embedded user summary
    private String userId;
    private String userName;
    private String userEmail;
    private String userPhone;

    public static BookingResponse from(Booking booking, Room room, User user) {
        BookingResponse r = new BookingResponse();

        r.id              = booking.getId();
        r.status          = booking.getStatus();
        r.checkInDate     = booking.getCheckInDate();
        r.checkOutDate    = booking.getCheckOutDate();
        r.guests          = booking.getGuests();
        r.specialRequests = booking.getSpecialRequests();
        r.createdAt       = booking.getCreatedAt();

        // Calculate nights and total price
        r.totalNights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        r.totalPrice  = r.totalNights * room.getPricePerNight();

        // Room info
        r.roomId       = room.getId();
        r.roomNumber   = room.getRoomNumber();
        r.roomType     = room.getType();
        r.pricePerNight = room.getPricePerNight();
        r.roomImages   = room.getImages();

        // User info
        r.userId    = user.getId();
        r.userName  = user.getName();
        r.userEmail = user.getEmail();
        r.userPhone = user.getPhone();

        return r;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String getId() { return id; }
    public String getStatus() { return status; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public int getGuests() { return guests; }
    public String getSpecialRequests() { return specialRequests; }
    public long getTotalNights() { return totalNights; }
    public double getTotalPrice() { return totalPrice; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getRoomId() { return roomId; }
    public String getRoomNumber() { return roomNumber; }
    public String getRoomType() { return roomType; }
    public double getPricePerNight() { return pricePerNight; }
    public List<String> getRoomImages() { return roomImages; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public String getUserPhone() { return userPhone; }
}

