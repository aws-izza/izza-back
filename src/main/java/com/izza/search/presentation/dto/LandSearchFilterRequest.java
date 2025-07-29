package com.izza.search.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "토지 검색 필터 요청")
public record LandSearchFilterRequest(
        @Schema(description = "최소 토지 면적 (㎡)", example = "50.0")
        Double landAreaMin,
        
        @Schema(description = "최대 토지 면적 (㎡)", example = "500.0")
        Double landAreaMax,
        
        @Schema(description = "최소 공시지가 (원/㎡)", example = "1000000")
        Double officialLandPriceMin,
        
        @Schema(description = "최대 공시지가 (원/㎡)", example = "10000000")
        Double officialLandPriceMax,
        
        @Schema(description = "지목 코드 목록 (0:지정되지않음, 1:전, 2:답, 8:대, 9:공장용지 등)", 
                example = "[1, 2, 8]")
        List<Integer> landCategoryCodes,
        
        @Schema(description = "농업용 토지만 검색", example = "true")
        Boolean agriculturalOnly,
        
        @Schema(description = "건축 가능 토지만 검색", example = "true")
        Boolean buildableOnly
) {
}
