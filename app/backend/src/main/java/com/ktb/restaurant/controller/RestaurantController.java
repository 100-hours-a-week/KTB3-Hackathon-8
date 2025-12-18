package com.ktb.restaurant.controller;

import com.ktb.restaurant.google.dto.PlaceSummaryDto;
import com.ktb.restaurant.google.service.RestaurantSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Restaurant", description = "레스토랑 검색 API")
@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantController {

    private final RestaurantSearchService restaurantSearchService;

    public RestaurantController(RestaurantSearchService restaurantSearchService) {
        this.restaurantSearchService = restaurantSearchService;
    }

    @Operation(
        summary = "역 주변 레스토랑 검색",
        description = "지정된 역 주변의 레스토랑을 검색합니다. (반경 500m 내)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "검색 성공"),
        @ApiResponse(responseCode = "400", description = "역 정보를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/search")
    public ResponseEntity<List<PlaceSummaryDto>> searchRestaurantsByStation(
        @Parameter(description = "역 이름 (예: 판교, 강남)", required = true)
        @RequestParam String station
    ) {
        List<PlaceSummaryDto> restaurants = restaurantSearchService.findRestaurantsByStation(station);
        return ResponseEntity.ok(restaurants);
    }
}
