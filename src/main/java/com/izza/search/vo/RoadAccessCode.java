package com.izza.search.vo;

import lombok.Getter;

/**
 * 도로 접면 구분 코드 enum
 * 토지의 도로 접면 상황을 나타내는 코드
 */
@Getter
public enum RoadAccessCode {
    
    UNSPECIFIED(0, "지정되지않음", "도로 접면 상황이 지정되지 않음"),
    WIDE_ROAD_ONE_SIDE(1, "광대로한면", "광대로에 한 면이 접함"),
    WIDE_ROAD_CORNER_SMALL(2, "광대소각", "광대로 소각지에 접함"),
    WIDE_ROAD_CORNER_NARROW(3, "광대세각", "광대로 세각지에 접함"),
    MEDIUM_ROAD_ONE_SIDE(4, "중로한면", "중로에 한 면이 접함"),
    MEDIUM_ROAD_CORNER(5, "중로각지", "중로 각지에 접함"),
    SMALL_ROAD_ONE_SIDE(6, "소로한면", "소로에 한 면이 접함"),
    SMALL_ROAD_CORNER(7, "소로각지", "소로 각지에 접함"),
    NARROW_ROAD_ONE_SIDE_GOOD(8, "세로한면(가)", "세로에 한 면이 접함(양호)"),
    NARROW_ROAD_CORNER_GOOD(9, "세로각지(가)", "세로 각지에 접함(양호)"),
    NARROW_ROAD_ONE_SIDE_POOR(10, "세로한면(불)", "세로에 한 면이 접함(불량)"),
    NARROW_ROAD_CORNER_POOR(11, "세로각지(불)", "세로 각지에 접함(불량)"),
    NO_ROAD_ACCESS(12, "맹지", "도로에 접하지 않는 토지");
    
    private final int code;
    private final String name;
    private final String description;
    
    RoadAccessCode(int code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }
    
    public static RoadAccessCode fromCode(int code) {
        for (RoadAccessCode access : values()) {
            if (access.code == code) {
                return access;
            }
        }
        return UNSPECIFIED;
    }
    
    public boolean isIndustrialSuitable() {
        return this == WIDE_ROAD_ONE_SIDE || 
               this == WIDE_ROAD_CORNER_SMALL ||
               this == MEDIUM_ROAD_ONE_SIDE ||
               this == MEDIUM_ROAD_CORNER ||
               this == SMALL_ROAD_ONE_SIDE;
    }
    
    public boolean isGoodAccess() {
        return code >= 1 && code <= 8;
    }
}