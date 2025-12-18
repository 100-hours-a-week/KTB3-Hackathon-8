package com.ktb.restaurant.infra.google;

import com.ktb.restaurant.infra.google.dto.NearbySearchResponse;
import com.ktb.restaurant.infra.google.dto.TextSearchResponse;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class GooglePlacesApiClient {

    private static final String BASE_URL = "https://places.googleapis.com/v1";

    private final WebClient webClient;

    public GooglePlacesApiClient(
            @Value("${google.maps.api-key}") String apiKey
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("X-Goog-Api-Key", apiKey)
                .build();
    }

    /**
     * 1️⃣ 역 이름으로 장소 검색 (Text Search)
     */
    public TextSearchResponse searchStation(String stationName) {
        return webClient.post()
                .uri("/places:searchText")
                .header("X-Goog-FieldMask",
                        "places.id,places.displayName,places.location")
                .bodyValue(Map.of(
                        "textQuery", stationName
                ))
                .retrieve()
                .bodyToMono(TextSearchResponse.class)
                .block();
    }

    /**
     * 2️⃣ 좌표 기준 음식점 검색 (Nearby Search)
     */
    public NearbySearchResponse searchRestaurants(
            double lat, double lng, int radiusMeter
    ) {
        return webClient.post()
                .uri("/places:searchNearby")
                .header("X-Goog-FieldMask",
                        "places.id,places.displayName,places.formattedAddress,places.rating")
                .bodyValue(Map.of(
                        "locationRestriction", Map.of(
                                "circle", Map.of(
                                        "center", Map.of(
                                                "latitude", lat,
                                                "longitude", lng
                                        ),
                                        "radius", radiusMeter
                                )
                        ),
                        "includedTypes", List.of("restaurant")
                ))
                .retrieve()
                .bodyToMono(NearbySearchResponse.class)
                .block();
    }
}

