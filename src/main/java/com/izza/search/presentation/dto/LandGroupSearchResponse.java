package com.izza.search.presentation.dto;

import com.izza.search.vo.Point;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토지 그룹 검색 응답")
public record LandGroupSearchResponse(
        @Schema(description = "그룹 ID", example = "1")
        String id,
        
        @Schema(description = "그룹명", example = "강남구 역삼동")
        String name,
        
        @Schema(description = "그룹 내 토지 개수", example = "25")
        Long count,
        
        @Schema(description = "그룹 중심 좌표")
        Point point,

        @Schema(description = "마커 타입", example = "GROUP, LAND")
        String type
) {
}
