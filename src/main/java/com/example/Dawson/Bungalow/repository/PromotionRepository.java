package com.example.Dawson.Bungalow.repository;

import com.example.Dawson.Bungalow.model.Promotion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends MongoRepository<Promotion, String> {

    Optional<Promotion> findByPromoCode(String promoCode);

    List<Promotion> findAllByActiveTrue();

    List<Promotion> findAllByActiveTrueAndExpiryDateAfter(LocalDateTime now);

    boolean existsByPromoCode(String promoCode);
}
