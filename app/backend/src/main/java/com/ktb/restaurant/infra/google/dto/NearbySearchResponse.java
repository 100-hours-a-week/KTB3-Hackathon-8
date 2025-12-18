package com.ktb.restaurant.infra.google.dto;

import java.util.List;

public record NearbySearchResponse(
        List<GooglePlace> places
) {}
