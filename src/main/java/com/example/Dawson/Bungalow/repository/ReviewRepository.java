package com.example.Dawson.Bungalow.repository;

import com.example.Dawson.Bungalow.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    List<Review> findAllByOrderByCreatedAtDesc();

    List<Review> findByRating(int rating);

    List<Review> findByRatingGreaterThanEqual(int rating);

    long countByRating(int rating);

    @Aggregation(pipeline = {
            "{ $group: { _id: null, avgRating: { $avg: '$rating' } } }"
    })
    AverageResult findAverageRating();

    interface AverageResult {
        Double getAvgRating();
    }
}
