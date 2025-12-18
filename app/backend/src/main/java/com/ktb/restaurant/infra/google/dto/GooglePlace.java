package com.ktb.restaurant.infra.google.dto;

public record GooglePlace(
        String id,
        DisplayName displayName,
        Location location,
        String formattedAddress,
        Double rating
) {

    public record DisplayName(
            String text
    ) {}

    public record Location(
            double latitude,
            double longitude
    ) {}
}
