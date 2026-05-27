package com.example.Dawson.Bungalow.service;


import com.example.Dawson.Bungalow.dto.PromotionRequest;
import com.example.Dawson.Bungalow.model.Promotion;
import com.example.Dawson.Bungalow.repository.PromotionRepository;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFsOperations gridFsOperations;



    public Promotion createPromotion(PromotionRequest request, MultipartFile bannerImage) throws IOException {

        if (promotionRepository.existsByPromoCode(request.getPromoCode().toUpperCase())) {
            throw new IllegalArgumentException("Promo code '" + request.getPromoCode() + "' already exists.");
        }

        Promotion promotion = new Promotion();
        promotion.setTitle(request.getTitle());
        promotion.setPromoCode(request.getPromoCode());
        promotion.setDescription(request.getDescription());
        promotion.setDiscountPercentage(request.getDiscountPercentage());
        promotion.setExpiryDate(request.getExpiryDate());
        promotion.setActive(request.isActive());
        promotion.setCreatedBy(getCurrentUsername());

        if (bannerImage != null && !bannerImage.isEmpty()) {
            String imageId = storeBannerImage(bannerImage);
            promotion.setBannerImageId(imageId);
        }

        return promotionRepository.save(promotion);
    }


    public Promotion updatePromotion(String id, PromotionRequest request, MultipartFile bannerImage) throws IOException {

        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));


        if (!promotion.getPromoCode().equalsIgnoreCase(request.getPromoCode())) {
            if (promotionRepository.existsByPromoCode(request.getPromoCode().toUpperCase())) {
                throw new IllegalArgumentException("Promo code '" + request.getPromoCode() + "' already exists.");
            }
        }

        promotion.setTitle(request.getTitle());
        promotion.setPromoCode(request.getPromoCode());
        promotion.setDescription(request.getDescription());
        promotion.setDiscountPercentage(request.getDiscountPercentage());
        promotion.setExpiryDate(request.getExpiryDate());
        promotion.setActive(request.isActive());
        promotion.setUpdatedAt(LocalDateTime.now());

        if (bannerImage != null && !bannerImage.isEmpty()) {

            if (promotion.getBannerImageId() != null) {
                deleteBannerImage(promotion.getBannerImageId());
            }
            String imageId = storeBannerImage(bannerImage);
            promotion.setBannerImageId(imageId);
        }

        return promotionRepository.save(promotion);
    }


    public Promotion togglePromotion(String id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));

        promotion.setActive(!promotion.isActive());
        promotion.setUpdatedAt(LocalDateTime.now());
        return promotionRepository.save(promotion);
    }


    public void deletePromotion(String id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));

        if (promotion.getBannerImageId() != null) {
            deleteBannerImage(promotion.getBannerImageId());
        }

        promotionRepository.deleteById(id);
    }


    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }


    public List<Promotion> getActivePromotions() {
        return promotionRepository.findAllByActiveTrueAndExpiryDateAfter(LocalDateTime.now());
    }


    public Optional<Promotion> getById(String id) {
        return promotionRepository.findById(id);
    }


    public PromoValidationResult validatePromoCode(String code) {
        Optional<Promotion> opt = promotionRepository.findByPromoCode(code.toUpperCase().trim());

        if (opt.isEmpty()) {
            return new PromoValidationResult(false, 0, "Invalid promo code.");
        }

        Promotion promo = opt.get();

        if (!promo.isActive()) {
            return new PromoValidationResult(false, 0, "This promo code is currently inactive.");
        }

        if (promo.getExpiryDate().isBefore(LocalDateTime.now())) {
            return new PromoValidationResult(false, 0, "This promo code has expired.");
        }

        return new PromoValidationResult(true, promo.getDiscountPercentage(),
                "Promo code applied! You get " + promo.getDiscountPercentage() + "% off.");
    }


    public String storeBannerImage(MultipartFile file) throws IOException {
        String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
        ObjectId fileId = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), contentType);
        return fileId.toHexString();
    }


    public InputStream getBannerImage(String fileId) throws IOException {
        GridFSFile file = gridFsTemplate.findOne(
                new Query(Criteria.where("_id").is(new ObjectId(fileId)))
        );
        if (file == null) throw new RuntimeException("Banner image not found.");
        return gridFsOperations.getResource(file).getInputStream();
    }


    private void deleteBannerImage(String fileId) {
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(new ObjectId(fileId))));
    }


    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }


    public static class PromoValidationResult {
        private boolean valid;
        private double discountPercentage;
        private String message;

        public PromoValidationResult(boolean valid, double discountPercentage, String message) {
            this.valid = valid;
            this.discountPercentage = discountPercentage;
            this.message = message;
        }

        public boolean isValid() { return valid; }
        public double getDiscountPercentage() { return discountPercentage; }
        public String getMessage() { return message; }
    }
}
