package com.ktb.submission.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ktb.restaurant.google.dto.NearbySearchResponse;
import com.ktb.restaurant.google.dto.PlaceSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Python LLM 서버의 Restaurant 스키마에 맞춘 DTO
 */
@Getter
@AllArgsConstructor
public class RestaurantCandidate {

    private String displayName;          // 필수

    @JsonProperty("primaryType")
    private String primaryType;          // 메뉴유형 (optional)

    @JsonProperty("priceRange")
    private String priceRange;           // 가격대 (optional)

    private Double rating;               // 등급 (optional)

    @JsonProperty("goodForGroups")
    private Boolean goodForGroups;       // 모임에 좋은 장소 (optional)

    @JsonProperty("parkingOptions")
    private String parkingOptions;       // 주차 옵션 (optional)

    /**
     * PlaceSummaryDto를 Python Restaurant 스키마로 변환
     */
    public static RestaurantCandidate fromPlaceSummary(PlaceSummaryDto dto) {
        // priceRange 변환
        String priceRangeStr = null;
        if (dto.priceRange() != null && dto.priceRange().startPrice() != null) {
            String units = dto.priceRange().startPrice().units();
            priceRangeStr = units != null ? "₩".repeat(Math.min(Integer.parseInt(units) / 10000, 4)) : null;
        }

        // parkingOptions 변환
        String parkingStr = convertParkingOptions(dto.parkingOptions());

        return new RestaurantCandidate(
                dto.displayName(),
                dto.primaryType(),
                priceRangeStr,
                dto.rating(),
                dto.goodForGroups(),
                parkingStr
        );
    }

    private static String convertParkingOptions(NearbySearchResponse.ParkingOptions parking) {
        if (parking == null) {
            return null;
        }

        if (Boolean.TRUE.equals(parking.freeParkingLot()) ||
            Boolean.TRUE.equals(parking.freeGarageParking()) ||
            Boolean.TRUE.equals(parking.freeStreetParking())) {
            return "무료 주차 가능";
        }

        if (Boolean.TRUE.equals(parking.paidParkingLot()) ||
            Boolean.TRUE.equals(parking.paidGarageParking()) ||
            Boolean.TRUE.equals(parking.paidStreetParking())) {
            return "유료 주차 가능";
        }

        if (Boolean.TRUE.equals(parking.valetParking())) {
            return "발렛 주차 가능";
        }

        return null;
    }
}
