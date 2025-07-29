package com.izza.search.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토지 상세 정보 응답")
public record LandDetailResponse(
        @Schema(description = "토지 ID", example = "1")
        Long id,

        @Schema(description = "토지 주소", example = "서울특별시 강남구 역삼동 123-45")
        String address,
        
        @Schema(description = "토지 면적 (㎡)", example = "100.5")
        Double area,
        
        @Schema(description = "공시지가 (원/㎡)", example = "5000000")
        Long officialPrice,
        
        @Schema(description = "용도지역명", example = "일반상업지역")
        String useZoneName
) {
}
