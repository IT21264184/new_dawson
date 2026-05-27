package com.example.Dawson.Bungalow.controller;

import com.example.Dawson.Bungalow.dto.ReviewDTO;
import com.example.Dawson.Bungalow.model.Review;
import com.example.Dawson.Bungalow.repository.ReviewRepository;
import com.example.Dawson.Bungalow.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")

public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;

    @Autowired
    public ReviewController(ReviewService reviewService, ReviewRepository reviewRepository) {
        this.reviewService = reviewService;
        this.reviewRepository = reviewRepository;
    }

    // POST /api/reviews — Submit a new review
    @PostMapping
    public ResponseEntity<Review> submitReview(@Valid @RequestBody ReviewDTO dto) {
        Review saved = reviewService.submitReview(dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // GET /api/reviews — Get all reviews (newest first)
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    // GET /api/reviews/{id} — Get a single review by MongoDB ID
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable String id) {
        return reviewService.getReviewById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/reviews/{id} — Delete a review (admin only)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable String id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/reviews/stats — Average rating, total count, per-star breakdown
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("averageRating", reviewService.getAverageRating());
        stats.put("totalReviews", reviewService.getTotalReviews());

        // Per-star count breakdown (1 to 5)
        Map<Integer, Long> breakdown = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            breakdown.put(i, reviewRepository.countByRating(i));
        }
        stats.put("ratingBreakdown", breakdown);
        return ResponseEntity.ok(stats);
    }

    // GET /api/reviews/filter?minRating=4 — Filter by minimum rating
    @GetMapping("/filter")
    public ResponseEntity<List<Review>> filterByRating(@RequestParam(defaultValue = "1") int minRating) {
        return ResponseEntity.ok(reviewService.getReviewsByMinRating(minRating));
    }
}
