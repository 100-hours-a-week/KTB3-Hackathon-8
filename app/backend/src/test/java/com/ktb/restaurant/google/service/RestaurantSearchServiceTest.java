package com.ktb.restaurant.google.service;

import com.ktb.restaurant.google.api.GooglePlacesApiClient;
import com.ktb.restaurant.google.dto.NearbySearchResponse;
import com.ktb.restaurant.google.dto.PlaceSummaryDto;
import com.ktb.restaurant.google.dto.StationTextSearchResponse;
import com.ktb.restaurant.google.mapper.PlaceSummaryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantSearchService 테스트")
class RestaurantSearchServiceTest {

    @Mock
    private GooglePlacesApiClient googleClient;

    @Mock
    private PlaceSummaryMapper mapper;

    @InjectMocks
    private RestaurantSearchService restaurantSearchService;

    private StationTextSearchResponse.Place pangyo;
    private StationTextSearchResponse stationResponse;
    private NearbySearchResponse nearbyResponse;
    private PlaceSummaryDto restaurantDto;

    @BeforeEach
    void setUp() {
        // 판교역 위치 정보 (실제 좌표)
        NearbySearchResponse.LatLng pangyoLocation =
            new NearbySearchResponse.LatLng(37.3948, 127.1109);

        pangyo = new StationTextSearchResponse.Place(
            "경기도 성남시 분당구 백현동 판교역",
            pangyoLocation
        );

        stationResponse = new StationTextSearchResponse(List.of(pangyo));

        // Mock 레스토랑 데이터
        NearbySearchResponse.Place mockRestaurant = new NearbySearchResponse.Place(
            "restaurant",
            new NearbySearchResponse.LocalizedText("판교 맛집", "ko"),
            4.5,
            120,
            new NearbySearchResponse.LatLng(37.3950, 127.1110),
            "경기도 성남시 분당구 판교역로 123",
            new NearbySearchResponse.PriceRange(
                new NearbySearchResponse.PriceRange.Money("KRW", "10000", 0),
                new NearbySearchResponse.PriceRange.Money("KRW", "20000", 0)
            ),
            true,
            new NearbySearchResponse.ParkingOptions(true, false, false, false, false, false, false)
        );

        nearbyResponse = new NearbySearchResponse(List.of(mockRestaurant));

        restaurantDto = new PlaceSummaryDto(
            "restaurant",
            "판교 맛집",
            4.5,
            120,
            new NearbySearchResponse.LatLng(37.3950, 127.1110),
            "경기도 성남시 분당구 판교역로 123",
            new NearbySearchResponse.PriceRange(
                new NearbySearchResponse.PriceRange.Money("KRW", "10000", 0),
                new NearbySearchResponse.PriceRange.Money("KRW", "20000", 0)
            ),
            true,
            new NearbySearchResponse.ParkingOptions(true, false, false, false, false, false, false),
            4.3  // 계산된 베이지안 평점
        );
    }

    @Test
    @DisplayName("판교역 주변 레스토랑 검색 성공")
    void findRestaurantsByStation_Success() {
        // given
        String stationName = "판교";
        given(googleClient.searchStation("판교역")).willReturn(stationResponse);
        given(googleClient.searchRestaurants(37.3948, 127.1109, 500))
            .willReturn(nearbyResponse);
        given(mapper.toDto(any(NearbySearchResponse.Place.class)))
            .willReturn(restaurantDto);

        // when
        List<PlaceSummaryDto> results = restaurantSearchService.findRestaurantsByStation(stationName);

        // then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);

        PlaceSummaryDto result = results.get(0);
        assertThat(result.displayName()).isEqualTo("판교 맛집");
        assertThat(result.rating()).isEqualTo(4.5);
        assertThat(result.userRatingCount()).isEqualTo(120);
        assertThat(result.formattedAddress()).contains("판교역로");
        assertThat(result.goodForGroups()).isTrue();
        assertThat(result.calcRating()).isEqualTo(4.3);

        // verify
        verify(googleClient).searchStation("판교역");
        verify(googleClient).searchRestaurants(37.3948, 127.1109, 500);
        verify(mapper).toDto(any(NearbySearchResponse.Place.class));
    }

    @Test
    @DisplayName("역 정보를 찾을 수 없는 경우 예외 발생")
    void findRestaurantsByStation_StationNotFound() {
        // given
        String stationName = "존재하지않는역";
        StationTextSearchResponse emptyResponse = new StationTextSearchResponse(List.of());
        given(googleClient.searchStation("존재하지않는역역")).willReturn(emptyResponse);

        // when & then
        assertThatThrownBy(() -> restaurantSearchService.findRestaurantsByStation(stationName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("역 정보를 찾을 수 없습니다");

        verify(googleClient).searchStation("존재하지않는역역");
    }

    @Test
    @DisplayName("역은 찾았지만 위치 정보가 없는 경우 예외 발생")
    void findRestaurantsByStation_LocationNull() {
        // given
        String stationName = "판교";
        StationTextSearchResponse.Place stationWithoutLocation =
            new StationTextSearchResponse.Place("판교역", null);
        StationTextSearchResponse invalidResponse =
            new StationTextSearchResponse(List.of(stationWithoutLocation));
        given(googleClient.searchStation("판교역")).willReturn(invalidResponse);

        // when & then
        assertThatThrownBy(() -> restaurantSearchService.findRestaurantsByStation(stationName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("역 정보를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("주변에 레스토랑이 없는 경우 빈 리스트 반환")
    void findRestaurantsByStation_NoRestaurants() {
        // given
        String stationName = "판교";
        NearbySearchResponse emptyNearbyResponse = new NearbySearchResponse(null);

        given(googleClient.searchStation("판교역")).willReturn(stationResponse);
        given(googleClient.searchRestaurants(37.3948, 127.1109, 500))
            .willReturn(emptyNearbyResponse);

        // when
        List<PlaceSummaryDto> results = restaurantSearchService.findRestaurantsByStation(stationName);

        // then
        assertThat(results).isEmpty();
        verify(googleClient).searchStation("판교역");
        verify(googleClient).searchRestaurants(37.3948, 127.1109, 500);
    }

    @Test
    @DisplayName("여러 레스토랑 검색 결과 매핑")
    void findRestaurantsByStation_MultipleRestaurants() {
        // given
        String stationName = "판교";

        NearbySearchResponse.Place restaurant1 = new NearbySearchResponse.Place(
            "restaurant",
            new NearbySearchResponse.LocalizedText("판교 맛집1", "ko"),
            4.5, 120,
            new NearbySearchResponse.LatLng(37.3950, 127.1110),
            "주소1", null, true, null
        );

        NearbySearchResponse.Place restaurant2 = new NearbySearchResponse.Place(
            "restaurant",
            new NearbySearchResponse.LocalizedText("판교 맛집2", "ko"),
            4.2, 80,
            new NearbySearchResponse.LatLng(37.3951, 127.1111),
            "주소2", null, false, null
        );

        NearbySearchResponse multipleResponse = new NearbySearchResponse(
            List.of(restaurant1, restaurant2)
        );

        PlaceSummaryDto dto1 = new PlaceSummaryDto(
            "restaurant", "판교 맛집1", 4.5, 120,
            new NearbySearchResponse.LatLng(37.3950, 127.1110),
            "주소1", null, true, null, 4.3
        );

        PlaceSummaryDto dto2 = new PlaceSummaryDto(
            "restaurant", "판교 맛집2", 4.2, 80,
            new NearbySearchResponse.LatLng(37.3951, 127.1111),
            "주소2", null, false, null, 4.0
        );

        given(googleClient.searchStation("판교역")).willReturn(stationResponse);
        given(googleClient.searchRestaurants(37.3948, 127.1109, 500))
            .willReturn(multipleResponse);
        given(mapper.toDto(restaurant1)).willReturn(dto1);
        given(mapper.toDto(restaurant2)).willReturn(dto2);

        // when
        List<PlaceSummaryDto> results = restaurantSearchService.findRestaurantsByStation(stationName);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).displayName()).isEqualTo("판교 맛집1");
        assertThat(results.get(1).displayName()).isEqualTo("판교 맛집2");
    }
}
