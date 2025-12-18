package com.ktb.restaurant.infra.google.dto;

import java.util.List;

public record TextSearchResponse(
        List<GooglePlace> places
) {

    public GooglePlace getFirstPlace() {
        return (places == null || places.isEmpty()) ? null : places.get(0);
    }
}
