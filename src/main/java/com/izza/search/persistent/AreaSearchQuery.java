package com.izza.search.persistent;

import com.izza.search.domain.ZoomLevel;
import com.izza.search.vo.Point;

/**
 * 행정구역 검색 요청 DTO (Persistent 계층용)
 */
public record AreaSearchQuery(
        ZoomLevel zoomLevel,
        Point southWest,
        Point northEast
) {
}