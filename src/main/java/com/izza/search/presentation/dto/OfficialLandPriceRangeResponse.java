package com.izza.search.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공시지가 범위 응답")
public record OfficialLandPriceRangeResponse(
        @Schema(description = "최소 공시지가 (원/㎡", example = "500000")
        double min,
        
        @Schema(description = "최대 공시지가 (원/㎡)", example = "20000000")
        double max
) {

}

