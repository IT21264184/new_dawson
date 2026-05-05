package com.example.Dawson.Bungalow.repository;

import com.example.Dawson.Bungalow.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    // Find all reviews ordered by newest first
    List<Review> findAllByOrderByCreatedAtDesc();

    // Find reviews by exact rating
    List<Review> findByRating(int rating);

    // Find reviews by minimum rating
    List<Review> findByRatingGreaterThanEqual(int rating);

    // Count reviews by a specific rating value
    long countByRating(int rating);

    // Get average rating using MongoDB aggregation
    @Aggregation(pipeline = {
            "{ $group: { _id: null, avgRating: { $avg: '$rating' } } }"
    })
    AverageResult findAverageRating();

    // Inner interface to capture aggregation result
    interface AverageResult {
        Double getAvgRating();
    }
}
