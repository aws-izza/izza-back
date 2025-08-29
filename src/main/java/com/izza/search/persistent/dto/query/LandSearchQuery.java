package com.izza.search.persistent.dto.query;

import java.util.List;

/**
 * 토지 검색을 위한 통합 쿼리 DTO
 */
public record LandSearchQuery(
        // 지도 영역 필터
        Double southWestLng,
        Double southWestLat,
        Double northEastLng,
        Double northEastLat,
        
        // 토지 속성 필터
        Long landAreaMin,
        Long landAreaMax,
        Long officialLandPriceMin,
        Long officialLandPriceMax,
        List<String> useZoneCategories
) {
    
    /**
     * 지도 영역 필터가 설정되어 있는지 확인
     */
    public boolean hasMapBounds() {
        return southWestLat != null && southWestLng != null &&
               northEastLat != null && northEastLng != null;
    }
    
    /**
     * 토지 면적 필터가 설정되어 있는지 확인
     */
    public boolean hasAreaFilter() {
        return landAreaMin != null || landAreaMax != null;
    }
    
    /**
     * 공시지가 필터가 설정되어 있는지 확인
     */
    public boolean hasPriceFilter() {
        return officialLandPriceMin != null || officialLandPriceMax != null;
    }
    
    /**
     * 용도지역 필터가 설정되어 있는지 확인
     */
    public boolean hasUseZoneFilter() {
        return useZoneCategories != null && !useZoneCategories.isEmpty();
    }
}