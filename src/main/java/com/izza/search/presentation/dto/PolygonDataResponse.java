package com.izza.search.presentation.dto;

import com.izza.search.vo.Point;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "폴리곤 데이터 응답")
public record PolygonDataResponse(
                @Schema(description = "폴리곤을 구성하는 좌표점들의 리스트") List<List<Point>> polygon) {
}