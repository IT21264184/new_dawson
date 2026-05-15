package com.example.Dawson.Bungalow.controller;

import com.example.Dawson.Bungalow.dto.PromotionRequest;
import com.example.Dawson.Bungalow.model.Promotion;
import com.example.Dawson.Bungalow.service.PromotionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promotions")
@CrossOrigin(origins = "*")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    // ═══════════════════════════════════════════════════════════════════════════
    //  PUBLIC ENDPOINTS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/promotions/active
     * Returns all active, non-expired promotions (for the guest-facing page)
     */
    @GetMapping("/active")
    public ResponseEntity<List<Promotion>> getActivePromotions() {
        return ResponseEntity.ok(promotionService.getActivePromotions());
    }

    /**
     * POST /api/promotions/validate
     * Validates a promo code entered by a guest during booking
     * Body: { "promoCode": "SUMMER20" }
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validatePromoCode(@RequestBody Map<String, String> body) {
        String code = body.get("promoCode");
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Promo code is required."));
        }

        PromotionService.PromoValidationResult result = promotionService.validatePromoCode(code);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", result.isValid());
        response.put("discountPercentage", result.getDiscountPercentage());
        response.put("message", result.getMessage());
        response.put("promoCode", code.toUpperCase());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/promotions/{id}/banner
     * Streams the banner image from GridFS (public, no auth needed)
     */
    @GetMapping("/{id}/banner")
    public ResponseEntity<byte[]> getBannerImage(@PathVariable String id) {
        Promotion promotion = promotionService.getById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        if (promotion.getBannerImageId() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            InputStream stream = promotionService.getBannerImage(promotion.getBannerImageId());
            byte[] imageBytes = StreamUtils.copyToByteArray(stream);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); // or detect from GridFS metadata
            headers.setContentLength(imageBytes.length);

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  ADMIN ENDPOINTS  (require ADMIN role via JWT)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/promotions
     * Admin: get all promotions (including inactive/expired)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Promotion>> getAllPromotions() {
        return ResponseEntity.ok(promotionService.getAllPromotions());
    }



    /**
     * POST /api/promotions
     * Admin: create a promotion with optional banner image
     * Content-Type: multipart/form-data
     *   - Fields: title, promoCode, description, discountPercentage, expiryDate, active
     *   - File:   bannerImage (optional)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createPromotion(
            @RequestParam("title") String title,
            @RequestParam("promoCode") String promoCode,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("discountPercentage") Double discountPercentage,
            @RequestParam("expiryDate") String expiryDate,
            @RequestParam(value = "isActive", defaultValue = "true") boolean active,
            @RequestPart(value = "image", required = false) MultipartFile bannerImage) {
        try {
            PromotionRequest request = new PromotionRequest();
            request.setTitle(title);
            request.setPromoCode(promoCode);
            request.setDescription(description);
            request.setDiscountPercentage(discountPercentage);
            request.setExpiryDate(LocalDateTime.parse(expiryDate));  // "2025-12-31T00:00"
            request.setActive(active);

            Promotion created = promotionService.createPromotion(request, bannerImage);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to upload banner image."));
        }
    }

    /**
     * PUT /api/promotions/{id}
     * Admin: update a promotion
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePromotion(
            @PathVariable String id,
            @Valid @RequestPart("data") PromotionRequest request,
            @RequestPart(value = "bannerImage", required = false) MultipartFile bannerImage) {
        try {
            Promotion updated = promotionService.updatePromotion(id, request, bannerImage);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to upload banner image."));
        }
    }

    /**
     * PATCH /api/promotions/{id}/toggle
     * Admin: enable or disable a promotion
     */
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> togglePromotion(@PathVariable String id) {
        try {
            Promotion toggled = promotionService.togglePromotion(id);
            Map<String, Object> response = new HashMap<>();
            response.put("id", toggled.getId());
            response.put("active", toggled.isActive());
            response.put("message", "Promotion " + (toggled.isActive() ? "enabled" : "disabled") + " successfully.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/promotions/{id}
     * Admin: delete a promotion (also removes banner from GridFS)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePromotion(@PathVariable String id) {
        try {
            promotionService.deletePromotion(id);
            return ResponseEntity.ok(Map.of("message", "Promotion deleted successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/promotions/{id}/banner
     * Admin: upload or replace only the banner image
     */
    @PostMapping(value = "/{id}/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadBanner(
            @PathVariable String id,
            @RequestPart("bannerImage") MultipartFile bannerImage) {
        try {
            Promotion promotion = promotionService.getById(id)
                    .orElseThrow(() -> new RuntimeException("Promotion not found"));

            String imageId = promotionService.storeBannerImage(bannerImage);
            promotion.setBannerImageId(imageId);

            return ResponseEntity.ok(Map.of(
                    "message", "Banner uploaded successfully.",
                    "bannerImageId", imageId,
                    "bannerUrl", "/api/promotions/" + id + "/banner"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to upload image."));
        }
    }
}
