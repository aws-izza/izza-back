package com.izza.search.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "용도지역 카테고리 DTO")
public record UseZoneCategoryResponse(
        @Schema(description = "카테고리 이름", example = "COMMERCIAL")
        String name,
        
        @Schema(description = "카테고리 표시명", example = "상업지역")
        String displayName
) {
}