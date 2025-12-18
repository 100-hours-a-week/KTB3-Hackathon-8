package com.ktb.restaurant.google.mapper;

import com.ktb.restaurant.google.dto.NearbySearchResponse;
import com.ktb.restaurant.google.dto.PlaceSummaryDto;
import com.ktb.restaurant.google.rating.RatingCalculator;
import org.springframework.stereotype.Component;

@Component
public class PlaceSummaryMapper {

    private final RatingCalculator ratingCalculator;

    public PlaceSummaryMapper(RatingCalculator ratingCalculator) {
        this.ratingCalculator = ratingCalculator;
    }

    public PlaceSummaryDto toDto(NearbySearchResponse.Place p) {
        String name = (p.displayName() == null) ? null : p.displayName().text();
        double calc = ratingCalculator.calcRating(p.rating(), p.userRatingCount());

        return new PlaceSummaryDto(
                p.primaryType(),
                name,
                p.rating(),
                p.userRatingCount(),
                p.location(),
                p.formattedAddress(),
                p.priceRange(),
                p.goodForGroups(),
                p.parkingOptions(),
                calc
        );
    }
}
