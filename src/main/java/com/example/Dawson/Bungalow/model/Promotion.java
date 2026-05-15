package com.example.Dawson.Bungalow.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Document(collection = "promotions")
public class Promotion {

    @Id
    private String id;

    private String title;

    @Indexed(unique = true)
    private String promoCode;          // e.g. "SUMMER20"

    private String description;

    private double discountPercentage; // e.g. 20.0

    private String bannerImageId;      // GridFS file ID for the banner image

    private LocalDateTime expiryDate;

    private boolean active;            // Admin can enable/disable

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;          // Admin username/id who created it

    // ─── Constructors ───────────────────────────────────────────────────────────

    public Promotion() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.active = true;
    }

    // ─── Getters & Setters ──────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode.toUpperCase().trim(); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }

    public String getBannerImageId() { return bannerImageId; }
    public void setBannerImageId(String bannerImageId) { this.bannerImageId = bannerImageId; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
