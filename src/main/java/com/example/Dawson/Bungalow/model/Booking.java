package com.example.Dawson.Bungalow.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "bookings")
public class Booking {

    @Id
    private String id;
    private String userId;
    private String roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int guests;
    private String status = "pending";
    private String specialRequests;
    private Double totalPrice;        // ← new
    private String promoCode;         // ← new
    private LocalDateTime createdAt = LocalDateTime.now();

    public Booking() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
    public int getGuests() { return guests; }
    public void setGuests(int guests) { this.guests = guests; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
    public Double getTotalPrice() { return totalPrice; }          // ← new
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }  // ← new
    public String getPromoCode() { return promoCode; }            // ← new
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }      // ← new
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}