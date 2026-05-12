package com.example.Dawson.Bungalow.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "rooms")
public class Room {

    @Id
    private String id;

    @Indexed(unique = true)
    private String roomNumber;

    private String type;            // Single, Double, Deluxe, Suite

    private double pricePerNight;

    private int capacity;

    private String description;

    private List<String> images;

    private List<String> amenities;

    private boolean isActive = true;  // ← NEW: true by default, admin can deactivate

    private LocalDateTime createdAt = LocalDateTime.now();

    // NOTE: No isAvailable field.
    // Availability is calculated dynamically by checking whether any
    // confirmed or pending booking overlaps the requested date range.

    public Room() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }

    public boolean isActive() { return isActive; }          // ← NEW
    public void setActive(boolean active) { isActive = active; }  // ← NEW

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}