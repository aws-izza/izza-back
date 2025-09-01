package com.izza.analysis.vo;

import lombok.Getter;

/**
 * 분석 통계 유형을 구분하는 열거형
 */
@Getter
public enum AnalysisStatisticsType {
    
    /** 토지 면적 통계 (㎡) */
    LAND_AREA(AnalysisCategory.LOCATION_CONDITION, "토지면적"),
    
    /** 공시지가 통계 (원/㎡) */
    OFFICIAL_LAND_PRICE(AnalysisCategory.LOCATION_CONDITION, "공시지가"),
    
    /** 용도지역 통계 */
    USE_DISTRICT(AnalysisCategory.LOCATION_CONDITION, "용도지역"),
    
    /** 전기 요금 통계 (원/kWh) */
    ELECTRICITY_COST(AnalysisCategory.LOCATION_CONDITION, "전기요금"),
    
    /** 변전소 개수 통계 (개) */
    SUBSTATION_COUNT(AnalysisCategory.SAFETY, "변전소개수"),
    
    /** 송전탑 개수 통계 (개) */
    TRANSMISSION_TOWER_COUNT(AnalysisCategory.INFRASTRUCTURE, "송전탑개수"),

    /** 인구 밀도 통계 (명/㎢) */
    POPULATION_DENSITY(AnalysisCategory.INFRASTRUCTURE, "인구밀도"),

    /** 전기선 개수 통계 (개) */
    TRANSMISSION_LINE_COUNT(AnalysisCategory.SAFETY, "전기선개수"),
    
    /** 재해 발생 통계 (건) */
    DISASTER_COUNT(AnalysisCategory.SAFETY, "재해발생");
    
    private final AnalysisCategory category;
    private final String displayName;
    
    AnalysisStatisticsType(AnalysisCategory category, String displayName) {
        this.category = category;
        this.displayName = displayName;
    }

    /**
     * 분석 카테고리별 분류 열거형
     */
    @Getter
    public enum AnalysisCategory {
        /** 입지 조건 */
        LOCATION_CONDITION("입지조건"),
        
        /** 인프라 */
        INFRASTRUCTURE("인프라"),
        
        /** 안정성 */
        SAFETY("안정성");
        
        private final String displayName;
        
        AnalysisCategory(String displayName) {
            this.displayName = displayName;
        }
    }
}