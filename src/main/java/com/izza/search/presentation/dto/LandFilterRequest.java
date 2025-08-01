package com.izza.search.presentation.dto;

import java.util.List;

/**
 * 토지 필터 요청 DTO (Persistent 계층용)
 */
public record LandFilterRequest(
        Double landAreaMin,
        Double landAreaMax,
        Double officialLandPriceMin,
        Double officialLandPriceMax,
        List<Integer> landCategoryCodes,
        Boolean agriculturalOnly,
        Boolean buildableOnly
) {
    
    /**
     * 면적 필터가 설정되어 있는지 확인
     */
    public boolean hasAreaFilter() {
        return landAreaMin != null || landAreaMax != null;
    }
    
    /**
     * 가격 필터가 설정되어 있는지 확인
     */
    public boolean hasPriceFilter() {
        return officialLandPriceMin != null || officialLandPriceMax != null;
    }
    
    /**
     * 지목 필터가 설정되어 있는지 확인
     */
    public boolean hasCategoryFilter() {
        return (landCategoryCodes != null && !landCategoryCodes.isEmpty()) ||
               Boolean.TRUE.equals(agriculturalOnly) ||
               Boolean.TRUE.equals(buildableOnly);
    }
    
    /**
     * 어떤 필터라도 설정되어 있는지 확인
     */
    public boolean hasAnyFilter() {
        return hasAreaFilter() || hasPriceFilter() || hasCategoryFilter();
    }
}