package com.izza.search.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Long 타입 범위 DTO")
public record LongRangeDto(
        @Schema(description = "최솟값", example = "0")
        long min,
        
        @Schema(description = "최댓값", example = "1000")
        long max
) {
}