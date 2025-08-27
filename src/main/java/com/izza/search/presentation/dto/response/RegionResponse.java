package com.izza.search.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시도/시군구 지역 DTO")
public record RegionResponse(
        @Schema(description = "지역 코드", example = "11000000")
        String code,
        
        @Schema(description = "지역 이름", example = "서울시")
        String name
) {
}