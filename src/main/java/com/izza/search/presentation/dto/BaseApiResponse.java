package com.izza.search.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "기본 API 응답 래퍼")
public class BaseApiResponse<T> {
    @Schema(description = "응답 데이터")
    T data;

    public static <T> BaseApiResponse<T> ok() {
        return new BaseApiResponse<>();
    }

    public static <T> BaseApiResponse<T> ok(T data) {
        return new BaseApiResponse<>(data);
    }
}
