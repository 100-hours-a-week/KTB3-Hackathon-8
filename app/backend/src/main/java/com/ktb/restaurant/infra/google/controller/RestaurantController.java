package com.ktb.restaurant.infra.google.controller;

import com.ktb.restaurant.infra.google.dto.GooglePlace;
import com.ktb.restaurant.infra.google.service.RestaurantSearchService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantController {

    private final RestaurantSearchService service;

    public RestaurantController(RestaurantSearchService service) {
        this.service = service;
    }

    @GetMapping("/station")
    public List<GooglePlace> findByStation(
            @RequestParam String stationName
    ) {
        return service.findRestaurantsByStation(stationName);
    }
}
