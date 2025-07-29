package com.izza.search.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "숫자 범위 DTO")
public record NumberRangeDto(
        @Schema(description = "최솟값", example = "0.0")
        double min,
        
        @Schema(description = "최댓값", example = "1000.0")
        double max
) {
}
