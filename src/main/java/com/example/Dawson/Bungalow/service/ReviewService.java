package com.example.Dawson.Bungalow.service;


import com.example.Dawson.Bungalow.dto.ReviewDTO;
import com.example.Dawson.Bungalow.model.Review;
import com.example.Dawson.Bungalow.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    // Submit a new review
    public Review submitReview(ReviewDTO dto) {
        Review review = new Review(dto.getName(), dto.getRating(), dto.getComment());
        return reviewRepository.save(review);
    }

    // Get all reviews (newest first)
    public List<Review> getAllReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    // Get a single review by ID
    public Optional<Review> getReviewById(String id) {
        return reviewRepository.findById(id);
    }

    // Delete a review by ID
    public void deleteReview(String id) {
        if (!reviewRepository.existsById(id)) {
            throw new RuntimeException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }

    // Get average rating (rounded to 1 decimal)
    public double getAverageRating() {
        ReviewRepository.AverageResult result = reviewRepository.findAverageRating();
        if (result == null || result.getAvgRating() == null) return 0.0;
        return Math.round(result.getAvgRating() * 10.0) / 10.0;
    }

    // Get total review count
    public long getTotalReviews() {
        return reviewRepository.count();
    }

    // Get reviews filtered by minimum rating
    public List<Review> getReviewsByMinRating(int minRating) {
        return reviewRepository.findByRatingGreaterThanEqual(minRating);
    }
}
