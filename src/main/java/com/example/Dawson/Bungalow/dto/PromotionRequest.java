package com.example.Dawson.Bungalow.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;


public class PromotionRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Promo code is required")
    @Pattern(regexp = "^[A-Za-z0-9]{3,20}$", message = "Promo code must be 3–20 alphanumeric characters")
    private String promoCode;

    private String description;

    @NotNull(message = "Discount percentage is required")
    @Min(value = 1, message = "Discount must be at least 1%")
    @Max(value = 100, message = "Discount cannot exceed 100%")
    private Double discountPercentage;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    private LocalDateTime expiryDate;

    private boolean active = true;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Double discountPercentage) { this.discountPercentage = discountPercentage; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}



class PromotionResponse {

    private String id;
    private String title;
    private String promoCode;
    private String description;
    private double discountPercentage;
    private String bannerImageUrl;
    private String expiryDate;
    private boolean active;
    private boolean expired;
    private String createdAt;


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }

    public String getBannerImageUrl() { return bannerImageUrl; }
    public void setBannerImageUrl(String bannerImageUrl) { this.bannerImageUrl = bannerImageUrl; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isExpired() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}




class PromoCodeValidationRequest {

    @NotBlank(message = "Promo code is required")
    private String promoCode;

    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
}




class PromoCodeValidationResponse {

    private boolean valid;
    private double discountPercentage;
    private String message;
    private String promoCode;

    public PromoCodeValidationResponse(boolean valid, double discountPercentage, String message, String promoCode) {
        this.valid = valid;
        this.discountPercentage = discountPercentage;
        this.message = message;
        this.promoCode = promoCode;
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
}