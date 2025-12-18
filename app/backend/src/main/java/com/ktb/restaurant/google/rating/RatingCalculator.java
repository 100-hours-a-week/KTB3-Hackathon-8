package com.ktb.restaurant.google.rating;

public interface RatingCalculator {
    double calcRating(Double rating, Integer userRatingCount);
}
