package com.ktb.restaurant.google.service;

import com.ktb.restaurant.google.api.GooglePlacesApiClient;
import com.ktb.restaurant.google.dto.NearbySearchResponse;
import com.ktb.restaurant.google.dto.PlaceSummaryDto;
import com.ktb.restaurant.google.dto.StationTextSearchResponse;
import com.ktb.restaurant.google.mapper.PlaceSummaryMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RestaurantSearchService {

    private final GooglePlacesApiClient googleClient;
    private final PlaceSummaryMapper mapper;

    public RestaurantSearchService(GooglePlacesApiClient googleClient, PlaceSummaryMapper mapper) {
        this.googleClient = googleClient;
        this.mapper = mapper;
    }

    public List<PlaceSummaryDto> findRestaurantsByStation(String stationName) {
        // 역 검색 (주소는 참고/표시용, Nearby는 location 사용)
        StationTextSearchResponse stationRes = googleClient.searchStation(stationName + "역");
        StationTextSearchResponse.Place station = stationRes.first();
        if (station == null || station.location() == null) {
            throw new IllegalArgumentException("역 정보를 찾을 수 없습니다: " + stationName);
        }

        double lat = station.location().latitude();
        double lng = station.location().longitude();

        NearbySearchResponse nearby = googleClient.searchRestaurants(lat, lng, 500);

        if (nearby.places() == null) return List.of();
        return nearby.places().stream()
                .map(mapper::toDto)
                .toList();
    }
}
