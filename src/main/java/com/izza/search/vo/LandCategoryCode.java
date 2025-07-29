package com.izza.search.vo;

import lombok.Getter;

/**
 * 지목 코드 enum
 * 토지의 주된 용도에 따른 법정 분류
 */
@Getter
public enum LandCategoryCode {
    
    UNSPECIFIED(0, "지정되지않음", "분류되지 않은 토지"),
    
    // 농업 관련 지목
    PADDY_FIELD(1, "전", "논으로 사용되는 토지"),
    DRY_FIELD(2, "답", "밭으로 사용되는 토지"),
    ORCHARD(3, "과수원", "과수를 재배하는 토지"),
    RANCH(4, "목장용지", "축산업을 위한 토지"),
    
    // 임업 관련 지목
    FOREST(5, "임야", "산림으로 이용되는 토지"),
    
    // 특수 용도 지목
    MINERAL_SPRING(6, "광천지", "온천이나 광천이 있는 토지"),
    SALT_FIELD(7, "염전", "소금을 생산하는 토지"),
    
    // 건축 관련 지목
    BUILDING_SITE(8, "대", "건물이 건축된 토지와 그 부속토지"),
    FACTORY_SITE(9, "공장용지", "공장 건물이 있는 토지"),
    SCHOOL_SITE(10, "학교용지", "학교 시설이 있는 토지"),
    
    // 교통 및 상업 시설
    PARKING_LOT(11, "주차장", "주차장으로 사용되는 토지"),
    GAS_STATION(12, "주유소용지", "주유소 시설이 있는 토지"),
    WAREHOUSE_SITE(13, "창고용지", "창고 건물이 있는 토지"),
    
    // 교통 인프라
    ROAD(14, "도로", "일반 교통에 사용되는 토지"),
    RAILWAY(15, "철도용지", "철도 시설이 있는 토지"),
    EMBANKMENT(16, "제방", "홍수 방지를 위한 제방 토지"),
    
    // 수자원 관련
    RIVER(17, "하천", "자연 또는 인공 하천 토지"),
    DITCH(18, "구거", "농업용수 등을 위한 수로"),
    POND(19, "유지", "연못이나 늪지"),
    FISH_FARM(20, "양어장", "물고기 양식을 위한 토지"),
    WATERWORKS(21, "수도용지", "상수도 시설이 있는 토지"),
    
    // 공공 및 문화 시설
    PARK(22, "공원", "공원으로 사용되는 토지"),
    SPORTS_FACILITY(23, "체육용지", "체육 시설이 있는 토지"),
    AMUSEMENT_PARK(24, "유원지", "유원지로 사용되는 토지"),
    RELIGIOUS_SITE(25, "종교용지", "종교 시설이 있는 토지"),
    HISTORIC_SITE(26, "사적지", "문화재나 사적이 있는 토지"),
    CEMETERY(27, "묘지", "매장을 위한 토지"),
    
    // 기타
    MISCELLANEOUS(28, "잡종지", "다른 지목에 속하지 않는 토지");
    
    private final int code;
    private final String name;
    private final String description;
    
    LandCategoryCode(int code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }
    
    /**
     * 코드로 지목을 찾는 메서드
     */
    public static LandCategoryCode fromCode(int code) {
        for (LandCategoryCode category : values()) {
            if (category.code == code) {
                return category;
            }
        }
        return UNSPECIFIED;
    }
    
    /**
     * 이름으로 지목을 찾는 메서드
     */
    public static LandCategoryCode fromName(String name) {
        if (name == null) {
            return UNSPECIFIED;
        }
        
        for (LandCategoryCode category : values()) {
            if (category.name.equals(name)) {
                return category;
            }
        }
        return UNSPECIFIED;
    }
    
    /**
     * 농업 관련 지목인지 확인
     */
    public boolean isAgricultural() {
        return this == PADDY_FIELD || this == DRY_FIELD || this == ORCHARD || this == RANCH;
    }
    
    /**
     * 건축 가능한 지목인지 확인
     */
    public boolean isBuildable() {
        return this == BUILDING_SITE || this == FACTORY_SITE || this == SCHOOL_SITE || 
               this == WAREHOUSE_SITE || this == GAS_STATION;
    }
    
    /**
     * 공공시설 지목인지 확인
     */
    public boolean isPublicFacility() {
        return this == SCHOOL_SITE || this == PARK || this == SPORTS_FACILITY || 
               this == ROAD || this == RAILWAY || this == WATERWORKS;
    }
    
    /**
     * 수자원 관련 지목인지 확인
     */
    public boolean isWaterRelated() {
        return this == RIVER || this == DITCH || this == POND || 
               this == FISH_FARM || this == WATERWORKS;
    }
    
    @Override
    public String toString() {
        return String.format("%s(%d)", name, code);
    }
}