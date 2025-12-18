package com.ktb.restaurant.google.api;

import com.ktb.restaurant.google.dto.NearbySearchResponse;
import com.ktb.restaurant.google.dto.StationTextSearchResponse;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class GooglePlacesApiClient {

    private static final String BASE_URL = "https://places.googleapis.com/v1";

    // 공백 금지(공식 문서 주의사항) :contentReference[oaicite:3]{index=3}
    private static final String STATION_FIELD_MASK =
            "places.formattedAddress,places.location";

    private static final String RESTAURANT_FIELD_MASK =
            "places.primaryType,places.displayName,places.rating,places.location,places.formattedAddress," +
                    "places.priceRange,places.goodForGroups,places.parkingOptions,places.userRatingCount";

    private final WebClient webClient;

    public GooglePlacesApiClient(@Value("${google.maps.api-key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("X-Goog-Api-Key", apiKey)
                .build();
    }

    // 1) 역 검색: formattedAddress “만 필요”하더라도 Nearby용 좌표 때문에 location은 최소로 같이 받음
    public StationTextSearchResponse searchStation(String stationQuery) {
        return webClient.post()
                .uri("/places:searchText")
                .header("X-Goog-FieldMask", STATION_FIELD_MASK)
                .bodyValue(Map.of("textQuery", stationQuery))
                .retrieve()
                .bodyToMono(StationTextSearchResponse.class)
                .block();
    }

    // 2) 주변 맛집
    public NearbySearchResponse searchRestaurants(double lat, double lng, int radiusMeter) {
        return webClient.post()
                .uri("/places:searchNearby")
                .header("X-Goog-FieldMask", RESTAURANT_FIELD_MASK)
                .bodyValue(Map.of(
                        "includedTypes", List.of("restaurant"),
                        "locationRestriction", Map.of(
                                "circle", Map.of(
                                        "center", Map.of("latitude", lat, "longitude", lng),
                                        "radius", radiusMeter
                                )
                        )
                ))
                .retrieve()
                .bodyToMono(NearbySearchResponse.class)
                .block();
    }
}
