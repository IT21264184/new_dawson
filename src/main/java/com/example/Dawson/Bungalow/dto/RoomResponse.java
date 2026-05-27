package com.example.Dawson.Bungalow.dto;

import com.example.Dawson.Bungalow.model.Room;

import java.time.LocalDateTime;
import java.util.List;

public class RoomResponse {

    private String id;
    private String roomNumber;
    private String type;
    private double pricePerNight;
    private int capacity;
    private String description;
    private List<String> images;
    private List<String> amenities;
    private LocalDateTime createdAt;
    private Boolean available;
    private boolean isActive;

    public static RoomResponse from(Room room, Boolean available, List<String> imageUrls) {
        RoomResponse r = new RoomResponse();
        r.id            = room.getId();
        r.roomNumber    = room.getRoomNumber();
        r.type          = room.getType();
        r.pricePerNight = room.getPricePerNight();
        r.capacity      = room.getCapacity();
        r.description   = room.getDescription();
        r.images        = imageUrls;
        r.amenities     = room.getAmenities();
        r.createdAt     = room.getCreatedAt();
        r.available     = available;
        r.isActive      = room.isActive();
        return r;
    }

    public String getId() { return id; }
    public String getRoomNumber() { return roomNumber; }
    public String getType() { return type; }
    public double getPricePerNight() { return pricePerNight; }
    public int getCapacity() { return capacity; }
    public String getDescription() { return description; }
    public List<String> getImages() { return images; }
    public List<String> getAmenities() { return amenities; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Boolean getAvailable() { return available; }
    public boolean isActive() { return isActive; }   // ← NEW
}