package com.example.Dawson.Bungalow.dto;

import java.util.List;

public class RoomRequest {

    private String roomNumber;
    private String type;
    private double pricePerNight;
    private int capacity;
    private String description;
    private List<String> images;
    private List<String> amenities;
    private boolean isActive = true;   // ← NEW

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
}