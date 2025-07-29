package com.izza.search.vo;

import lombok.Getter;

/**
 * 지형 고저 구분 코드 enum
 * 토지의 지형 고저 상황을 나타내는 코드
 */
@Getter
public enum TerrainHeightCode {
    
    LOW_LAND(1, "저지", "주변보다 낮은 지형"),
    FLAT_LAND(2, "평지", "평평한 지형"),
    GENTLE_SLOPE(3, "완경사", "완만한 경사지"),
    STEEP_SLOPE(4, "급경사", "급한 경사지"),
    HIGH_LAND(5, "고지", "주변보다 높은 지형");
    
    private final int code;
    private final String name;
    private final String description;
    
    TerrainHeightCode(int code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }
    
    public static TerrainHeightCode fromCode(int code) {
        for (TerrainHeightCode terrain : values()) {
            if (terrain.code == code) {
                return terrain;
            }
        }
        return FLAT_LAND; // 기본값
    }
    
    public boolean isIndustrialSuitable() {
        return this == FLAT_LAND || this == GENTLE_SLOPE;
    }
    
    public boolean isDevelopmentFriendly() {
        return this == FLAT_LAND || this == GENTLE_SLOPE;
    }
}