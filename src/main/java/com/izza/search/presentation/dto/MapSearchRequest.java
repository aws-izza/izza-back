package com.izza.search.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지도 영역 검색 요청")
public record MapSearchRequest(
        @Schema(description = "남서쪽 위도", example = "37.4979")
        Double southWestLat,
        
        @Schema(description = "남서쪽 경도", example = "127.0276")
        Double southWestLng,
        
        @Schema(description = "북동쪽 위도", example = "37.5665")
        Double northEastLat,
        
        @Schema(description = "북동쪽 경도", example = "127.0789")
        Double northEastLng,
        
        @Schema(description = "지도 줌 레벨", example = "14")
        Integer zoomLevel
) {
}
