package com.ktb.restaurant.google.dto;

import com.ktb.restaurant.google.dto.NearbySearchResponse.LatLng;
import java.util.List;

public record StationTextSearchResponse(
        List<Place> places
) {
    public Place first() {
        return (places == null || places.isEmpty()) ? null : places.get(0);
    }

    public record Place(
            String formattedAddress,
            LatLng location
    ) {}
}
