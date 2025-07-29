package com.izza.search.vo;

import lombok.Getter;

/**
 * 용도지구 코드 enum
 * 도시계획법에 따른 용도지구 분류
 */
@Getter
public enum UseDistrictCode {
    
    UNSPECIFIED(0, "지정되지않음", "분류되지 않은 용도지구", UseDistrictCategory.UNSPECIFIED),
    
    // 경관지구 (100번대)
    NATURAL_LANDSCAPE(110, "자연경관지구", "자연경관을 보호하기 위한 지구", UseDistrictCategory.LANDSCAPE),
    URBAN_LANDSCAPE(120, "시가지경관지구", "시가지의 경관을 관리하기 위한 지구", UseDistrictCategory.LANDSCAPE),
    SPECIALIZED_LANDSCAPE(130, "특화경관지구", "특별한 경관을 조성하기 위한 지구", UseDistrictCategory.LANDSCAPE),
    OTHER_LANDSCAPE(140, "기타경관지구", "기타 경관 관리를 위한 지구", UseDistrictCategory.LANDSCAPE),
    
    // 고도지구 (200번대)
    HEIGHT_DISTRICT(200, "고도지구", "건축물의 높이를 제한하는 지구", UseDistrictCategory.HEIGHT),
    
    // 방화지구 (300번대)
    FIRE_PREVENTION(300, "방화지구", "화재 확산을 방지하기 위한 지구", UseDistrictCategory.FIRE_PREVENTION),
    
    // 방재지구 (400번대)
    DISASTER_PREVENTION(400, "방재지구", "재해 예방을 위한 지구", UseDistrictCategory.DISASTER_PREVENTION),
    
    // 보호지구 (500번대)
    HISTORICAL_CULTURAL_PROTECTION(510, "역사문화환경보호지구", "역사문화환경을 보호하기 위한 지구", UseDistrictCategory.PROTECTION),
    IMPORTANT_FACILITY_PROTECTION(520, "중요시설물보호지구", "중요시설물을 보호하기 위한 지구", UseDistrictCategory.PROTECTION),
    PORT_FACILITY_PROTECTION(521, "중요시설물(항만)보호지구", "항만시설을 보호하기 위한 지구", UseDistrictCategory.PROTECTION),
    AIRPORT_FACILITY_PROTECTION(522, "중요시설물(공항)보호지구", "공항시설을 보호하기 위한 지구", UseDistrictCategory.PROTECTION),
    PUBLIC_FACILITY_PROTECTION(523, "중요시설물(공용)보호지구", "공용시설을 보호하기 위한 지구", UseDistrictCategory.PROTECTION),
    CORRECTIONAL_MILITARY_PROTECTION(524, "중요시설물(교정군사)보호지구", "교정 및 군사시설을 보호하기 위한 지구", UseDistrictCategory.PROTECTION),
    ECOSYSTEM_PROTECTION(530, "생태계보호지구", "생태계를 보호하기 위한 지구", UseDistrictCategory.PROTECTION),
    
    // 취락지구 (600번대)
    NATURAL_SETTLEMENT(610, "자연취락지구", "자연취락을 보전하기 위한 지구", UseDistrictCategory.SETTLEMENT),
    GROUP_SETTLEMENT(620, "집단취락지구", "집단취락을 관리하기 위한 지구", UseDistrictCategory.SETTLEMENT),
    
    // 개발진흥지구 (700번대)
    RESIDENTIAL_DEVELOPMENT(710, "주거개발진흥지구", "주거개발을 진흥하기 위한 지구", UseDistrictCategory.DEVELOPMENT),
    INDUSTRIAL_DISTRIBUTION_DEVELOPMENT(720, "산업유통개발진흥지구", "산업 및 유통개발을 진흥하기 위한 지구", UseDistrictCategory.DEVELOPMENT),
    TOURISM_RECREATION_DEVELOPMENT(730, "관광휴양개발진흥지구", "관광 및 휴양개발을 진흥하기 위한 지구", UseDistrictCategory.DEVELOPMENT),
    COMPLEX_DEVELOPMENT(740, "복합개발진흥지구", "복합개발을 진흥하기 위한 지구", UseDistrictCategory.DEVELOPMENT),
    SPECIFIC_DEVELOPMENT(750, "특정개발진흥지구", "특정개발을 진흥하기 위한 지구", UseDistrictCategory.DEVELOPMENT),
    
    // 특정용도제한지구 (800번대)
    SPECIFIC_USE_RESTRICTION(800, "특정용도제한지구", "특정 용도를 제한하는 지구", UseDistrictCategory.RESTRICTION),
    
    // 복합용도지구 (900번대)
    MIXED_USE(900, "복합용도지구", "복합적 용도로 사용하는 지구", UseDistrictCategory.MIXED_USE),
    
    // 기타지구 (999)
    OTHER_DISTRICT(999, "기타지구", "기타 용도지구", UseDistrictCategory.OTHER);
    
    private final int code;
    private final String name;
    private final String description;
    private final UseDistrictCategory category;
    
    UseDistrictCode(int code, String name, String description, UseDistrictCategory category) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.category = category;
    }
    
    /**
     * 코드로 용도지구를 찾는 메서드
     */
    public static UseDistrictCode fromCode(int code) {
        for (UseDistrictCode district : values()) {
            if (district.code == code) {
                return district;
            }
        }
        return UNSPECIFIED;
    }
    
    /**
     * 이름으로 용도지구를 찾는 메서드
     */
    public static UseDistrictCode fromName(String name) {
        if (name == null) {
            return UNSPECIFIED;
        }
        
        for (UseDistrictCode district : values()) {
            if (district.name.equals(name)) {
                return district;
            }
        }
        return UNSPECIFIED;
    }
    
    /**
     * 개발 관련 지구인지 확인
     */
    public boolean isDevelopmentDistrict() {
        return category == UseDistrictCategory.DEVELOPMENT;
    }
    
    /**
     * 보호 관련 지구인지 확인
     */
    public boolean isProtectionDistrict() {
        return category == UseDistrictCategory.PROTECTION;
    }
    
    /**
     * 경관 관련 지구인지 확인
     */
    public boolean isLandscapeDistrict() {
        return category == UseDistrictCategory.LANDSCAPE;
    }
    
    /**
     * 건축 제한이 있는 지구인지 확인
     */
    public boolean hasConstructionRestriction() {
        return category == UseDistrictCategory.PROTECTION || 
               category == UseDistrictCategory.HEIGHT || 
               category == UseDistrictCategory.RESTRICTION ||
               this == NATURAL_LANDSCAPE;
    }
    
    @Override
    public String toString() {
        return String.format("%s(%d)", name, code);
    }
    
    /**
     * 용도지구 대분류 enum
     */
    @Getter
    public enum UseDistrictCategory {
        UNSPECIFIED("미분류"),
        LANDSCAPE("경관지구"),
        HEIGHT("고도지구"),
        FIRE_PREVENTION("방화지구"),
        DISASTER_PREVENTION("방재지구"),
        PROTECTION("보호지구"),
        SETTLEMENT("취락지구"),
        DEVELOPMENT("개발진흥지구"),
        RESTRICTION("특정용도제한지구"),
        MIXED_USE("복합용도지구"),
        OTHER("기타지구");
        
        private final String displayName;
        
        UseDistrictCategory(String displayName) {
            this.displayName = displayName;
        }
    }
}