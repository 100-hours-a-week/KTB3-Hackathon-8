package com.ktb.restaurant.infra.google.service;

import com.ktb.restaurant.infra.google.GooglePlacesApiClient;
import com.ktb.restaurant.infra.google.dto.GooglePlace;
import com.ktb.restaurant.infra.google.dto.NearbySearchResponse;
import com.ktb.restaurant.infra.google.dto.TextSearchResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RestaurantSearchService {

    private final GooglePlacesApiClient googleClient;

    public RestaurantSearchService(GooglePlacesApiClient googleClient) {
        this.googleClient = googleClient;
    }

    public List<GooglePlace> findRestaurantsByStation(String stationName) {

        // 1️⃣ 역 검색
        TextSearchResponse stationResponse =
                googleClient.searchStation(stationName + "역");

        GooglePlace station = stationResponse.getFirstPlace();
        if (station == null) {
            throw new IllegalArgumentException("역 정보를 찾을 수 없습니다.");
        }

        double lat = station.location().latitude();
        double lng = station.location().longitude();

        // 2️⃣ 주변 맛집 검색
        NearbySearchResponse restaurantResponse =
                googleClient.searchRestaurants(lat, lng, 500);

        return restaurantResponse.places();
    }
}

