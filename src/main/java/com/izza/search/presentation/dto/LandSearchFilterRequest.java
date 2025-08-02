package com.izza.search.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "토지 검색 필터 요청")
public record LandSearchFilterRequest(
        @Schema(description = "최소 토지 면적 (㎡)", example = "50")
        Long landAreaMin,
        
        @Schema(description = "최대 토지 면적 (㎡)", example = "500")
        Long landAreaMax,
        
        @Schema(description = "최소 공시지가 (원/㎡)", example = "1000000")
        Long officialLandPriceMin,
        
        @Schema(description = "최대 공시지가 (원/㎡)", example = "10000000")
        Long officialLandPriceMax,
        
        @Schema(description = "용도지역 카테고리 목록 (COMMERCIAL, INDUSTRIAL, MANAGEMENT)", 
                example = "[\"COMMERCIAL\", \"INDUSTRIAL\"]")
        List<String> useZoneCategories
) {
}
