package com.izza.search.persistent;

import com.izza.search.vo.Point;

/**
 * 행정구역과 토지 개수 정보를 담는 DAO용 DTO
 */
public record AreaPolygonWithLandCount(
        String fullCode,
        String koreanName,
        String englishName,
        String type,
        Point centerPoint,
        Long landCount
) {
    
    /**
     * AreaPolygon과 토지 개수로 생성하는 팩토리 메서드
     */
    public static AreaPolygonWithLandCount of(AreaPolygon areaPolygon, Long landCount) {
        return new AreaPolygonWithLandCount(
                areaPolygon.getFullCode(),
                areaPolygon.getKoreanName(),
                areaPolygon.getEnglishName(),
                areaPolygon.getType(),
                areaPolygon.getCenterPoint(),
                landCount
        );
    }
}