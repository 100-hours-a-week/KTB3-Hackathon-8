package com.ktb.restaurant.google.dto;

public record PlaceSummaryDto(
        String primaryType,
        String displayName,
        Double rating,
        Integer userRatingCount,
        NearbySearchResponse.LatLng location,
        String formattedAddress,
        NearbySearchResponse.PriceRange priceRange,
        Boolean goodForGroups,
        NearbySearchResponse.ParkingOptions parkingOptions,
        Double calcRating
) {}
