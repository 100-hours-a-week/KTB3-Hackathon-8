package com.ktb.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;

@Schema(description = "공통 응답 구조")
public record CommonResponse<T>(
        @Schema(description = "실행 결과 message")
        @NotNull
        String message,
        @Schema(description = "응답 데이터")
        T data
) {
    public static CommonResponse<Void> of(String message) {
        return new CommonResponse<>(message, null);
    }

    public static <T> CommonResponse<T> of(String message, T data) {
        return new CommonResponse<>(message, data);
    }

    public static <T> CommonResponse<Collection<T>> of(String message, Collection<T> data) {
        return new CommonResponse<>(message, List.copyOf(data));
    }
}
