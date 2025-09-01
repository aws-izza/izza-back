package com.izza.search.persistent.dto.query;

import java.util.List;

/**
 * fullCode 기반 토지 검색을 위한 쿼리 DTO
 */
public record FullCodeLandCountQuery(
        // 법정동 코드 필터
        String fullCode,
        
        // 토지 속성 필터
        Long landAreaMin,
        Long landAreaMax,
        Long officialLandPriceMin,
        Long officialLandPriceMax,
        List<String> useZoneCategories
) {
    
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