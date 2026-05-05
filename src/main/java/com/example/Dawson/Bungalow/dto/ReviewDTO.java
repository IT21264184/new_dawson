package com.example.Dawson.Bungalow.dto;

import jakarta.validation.constraints.*;

public class ReviewDTO {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;

    @NotBlank(message = "Comment is required")
    @Size(min = 10, max = 1000, message = "Comment must be between 10 and 1000 characters")
    private String comment;

    // Constructors
    public ReviewDTO() {}

    public ReviewDTO(String name, int rating, String comment) {
        this.name = name;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
