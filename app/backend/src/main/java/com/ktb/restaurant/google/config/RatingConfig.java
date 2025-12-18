package com.ktb.restaurant.google.config;

import com.ktb.restaurant.google.rating.BayesianRatingCalculator;
import com.ktb.restaurant.google.rating.RatingCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RatingConfig {

    @Bean
    public RatingCalculator ratingCalculator() {
        // 예시값: 전체 평균 3.0, "리뷰 100개쯤부터 믿는다"
        return new BayesianRatingCalculator(3.0, 100);
    }
}
