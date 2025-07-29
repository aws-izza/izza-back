package com.izza.search.vo;

import lombok.Getter;

/**
 * 토지이용상황구분 코드 enum
 * 토지의 실제 이용 상황을 나타내는 코드
 */
@Getter
public enum LandUseStatusCode {
    
    // 주거용 (100번대)
    DETACHED_HOUSE(110, "단독", "단독주택", LandUseCategory.RESIDENTIAL),
    ROW_HOUSE(120, "연립", "연립주택", LandUseCategory.RESIDENTIAL),
    MULTI_FAMILY(130, "다세대", "다세대주택", LandUseCategory.RESIDENTIAL),
    APARTMENT(140, "아파트", "아파트", LandUseCategory.RESIDENTIAL),
    RESIDENTIAL_VACANT(150, "주거나지", "주거용 나대지", LandUseCategory.RESIDENTIAL),
    RESIDENTIAL_OTHER(160, "주거기타", "기타 주거용", LandUseCategory.RESIDENTIAL),
    
    // 상업·업무용 (200번대)
    COMMERCIAL_OFFICE(200, "상업.업무용", "상업·업무 복합용", LandUseCategory.COMMERCIAL),
    COMMERCIAL(210, "상업용", "상업용", LandUseCategory.COMMERCIAL),
    OFFICE(220, "업무용", "업무용", LandUseCategory.COMMERCIAL),
    COMMERCIAL_VACANT(230, "상업나지", "상업용 나대지", LandUseCategory.COMMERCIAL),
    COMMERCIAL_OTHER(240, "상업기타", "기타 상업용", LandUseCategory.COMMERCIAL),
    
    // 주상복합용 (300번대)
    MIXED_RESIDENTIAL_COMMERCIAL(300, "주.상복합용", "주거·상업 복합용", LandUseCategory.MIXED),
    RESIDENTIAL_COMMERCIAL(310, "주상용", "주상복합용", LandUseCategory.MIXED),
    RESIDENTIAL_COMMERCIAL_VACANT(320, "주상나지", "주상복합 나대지", LandUseCategory.MIXED),
    RESIDENTIAL_COMMERCIAL_OTHER(330, "주상기타", "기타 주상복합용", LandUseCategory.MIXED),
    
    // 공업용 (400번대)
    INDUSTRIAL_GENERAL(400, "공업용", "일반 공업용", LandUseCategory.INDUSTRIAL),
    INDUSTRIAL(410, "공업용", "공업용", LandUseCategory.INDUSTRIAL),
    INDUSTRIAL_VACANT(420, "공업나지", "공업용 나대지", LandUseCategory.INDUSTRIAL),
    INDUSTRIAL_OTHER(430, "공업기타", "기타 공업용", LandUseCategory.INDUSTRIAL),
    SOLAR_POWER_PLANT(440, "태양광발전소부지", "태양광발전소 부지", LandUseCategory.INDUSTRIAL),
    
    // 전 (500번대)
    PADDY_FIELD_GENERAL(500, "전", "일반 전", LandUseCategory.AGRICULTURAL),
    PADDY_FIELD(510, "전", "전", LandUseCategory.AGRICULTURAL),
    ORCHARD(520, "과수원", "과수원", LandUseCategory.AGRICULTURAL),
    PADDY_OTHER(530, "전기타", "기타 전", LandUseCategory.AGRICULTURAL),
    PADDY_WAREHOUSE(540, "전창고", "전 창고", LandUseCategory.AGRICULTURAL),
    PADDY_LIVESTOCK(550, "전축사", "전 축사", LandUseCategory.AGRICULTURAL),
    
    // 답 (600번대)
    DRY_FIELD_GENERAL(600, "답", "일반 답", LandUseCategory.AGRICULTURAL),
    DRY_FIELD(610, "답", "답", LandUseCategory.AGRICULTURAL),
    DRY_FIELD_OTHER(620, "답기타", "기타 답", LandUseCategory.AGRICULTURAL),
    DRY_FIELD_WAREHOUSE(630, "답창고", "답 창고", LandUseCategory.AGRICULTURAL),
    DRY_FIELD_LIVESTOCK(640, "답축사", "답 축사", LandUseCategory.AGRICULTURAL),
    
    // 임야 (700번대)
    FOREST_GENERAL(700, "임야", "일반 임야", LandUseCategory.FOREST),
    AFFORESTATION(710, "조림", "조림지", LandUseCategory.FOREST),
    NATURAL_FOREST(720, "자연림", "자연림", LandUseCategory.FOREST),
    FOREST_LAND(730, "토지임야", "토지 임야", LandUseCategory.FOREST),
    RANCH_LAND(740, "목장용지", "목장용지", LandUseCategory.FOREST),
    FOREST_OTHER(750, "임야기타", "기타 임야", LandUseCategory.FOREST),
    
    // 특수토지 (800번대)
    SPECIAL_LAND(800, "특수토지", "특수토지", LandUseCategory.SPECIAL),
    MINERAL_SPRING(810, "광천지", "광천지", LandUseCategory.SPECIAL),
    MINING_LAND(820, "광업용지", "광업용지", LandUseCategory.SPECIAL),
    SALT_FIELD(830, "염전", "염전", LandUseCategory.SPECIAL),
    FISH_FARM(831, "양어장·양식장", "양어장·양식장", LandUseCategory.SPECIAL),
    AMUSEMENT_PARK(840, "유원지", "유원지", LandUseCategory.SPECIAL),
    CAMPING_GROUND(841, "야영장", "야영장", LandUseCategory.SPECIAL),
    PARK_CEMETERY(850, "공원묘지", "공원묘지", LandUseCategory.SPECIAL),
    GOLF_COURSE(860, "골프장", "골프장", LandUseCategory.SPECIAL),
    SKI_RESORT(870, "스키장", "스키장", LandUseCategory.SPECIAL),
    HORSE_RACING_TRACK(880, "경마장", "경마장", LandUseCategory.SPECIAL),
    HORSE_RIDING_GROUND(881, "승마장", "승마장", LandUseCategory.SPECIAL),
    BUS_TERMINAL(890, "여객자동차터미널", "여객자동차터미널", LandUseCategory.SPECIAL),
    CONDOMINIUM(891, "콘도미니엄", "콘도미니엄", LandUseCategory.SPECIAL),
    AIRPORT(892, "공항", "공항", LandUseCategory.SPECIAL),
    HIGHWAY_REST_AREA(893, "고속도로휴게소", "고속도로휴게소", LandUseCategory.SPECIAL),
    POWER_PLANT(895, "발전소", "발전소", LandUseCategory.SPECIAL),
    LOGISTICS_TERMINAL(896, "물류터미널", "물류터미널", LandUseCategory.SPECIAL),
    SPECIAL_OTHER(899, "특수기타", "기타 특수토지", LandUseCategory.SPECIAL),
    
    // 공공용지등 (900번대)
    PUBLIC_LAND(900, "공공용지등", "공공용지 등", LandUseCategory.PUBLIC),
    ROAD_ETC(910, "도로등", "도로 등", LandUseCategory.PUBLIC),
    RIVER_ETC(920, "하천등", "하천 등", LandUseCategory.PUBLIC),
    PARK_ETC(930, "공원등", "공원 등", LandUseCategory.PUBLIC),
    SPORTS_FACILITY(940, "운동장등", "운동장 등", LandUseCategory.PUBLIC),
    PARKING_LOT_ETC(950, "주차장등", "주차장 등", LandUseCategory.PUBLIC),
    DANGEROUS_FACILITY(960, "위험시설", "위험시설", LandUseCategory.PUBLIC),
    HARMFUL_FACILITY(970, "유해.혐오시설", "유해·혐오시설", LandUseCategory.PUBLIC),
    OTHER(990, "기타", "기타", LandUseCategory.PUBLIC);
    
    private final int code;
    private final String name;
    private final String description;
    private final LandUseCategory category;
    
    LandUseStatusCode(int code, String name, String description, LandUseCategory category) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.category = category;
    }
    
    public static LandUseStatusCode fromCode(int code) {
        for (LandUseStatusCode status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return OTHER;
    }
    
    public boolean isIndustrialSuitable() {
        return category == LandUseCategory.INDUSTRIAL ||
               this == COMMERCIAL ||
               this == OFFICE ||
               this == LOGISTICS_TERMINAL ||
               this == POWER_PLANT ||
               this == INDUSTRIAL_VACANT ||
               this == COMMERCIAL_VACANT;
    }
    
    @Getter
    public enum LandUseCategory {
        RESIDENTIAL("주거용"),
        COMMERCIAL("상업용"),
        MIXED("복합용"),
        INDUSTRIAL("공업용"),
        AGRICULTURAL("농업용"),
        FOREST("임야"),
        SPECIAL("특수용지"),
        PUBLIC("공공용지");
        
        private final String displayName;
        
        LandUseCategory(String displayName) {
            this.displayName = displayName;
        }
    }
}