package com.izza.search.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지리적 좌표점")
public record Point(
        @Schema(description = "경도", example = "127.0276")
        double lng,
        
        @Schema(description = "위도", example = "37.4979")
        double lat
) {
}
