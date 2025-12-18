package com.ktb.restaurant.google.dto;

import java.util.List;

public record NearbySearchResponse(
        List<Place> places
) {
    public record Place(
            String primaryType,
            LocalizedText displayName,
            Double rating,
            Integer userRatingCount,
            LatLng location,
            String formattedAddress,
            PriceRange priceRange,
            Boolean goodForGroups,
            ParkingOptions parkingOptions
    ) {}

    public record LocalizedText(String text, String languageCode) {}

    public record LatLng(double latitude, double longitude) {}

    public record PriceRange(Money startPrice, Money endPrice) {
        public record Money(String currencyCode, String units, Integer nanos) {}
    }

    public record ParkingOptions(
            Boolean freeParkingLot,
            Boolean paidParkingLot,
            Boolean freeStreetParking,
            Boolean paidStreetParking,
            Boolean valetParking,
            Boolean freeGarageParking,
            Boolean paidGarageParking
    ) {}
}
