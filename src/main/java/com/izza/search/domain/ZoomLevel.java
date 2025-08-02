package com.izza.search.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ZoomLevel {
    SIDO(9, 14, "SIDO"),
    SIG(6, 8, "SIG"),
    EMD(4, 5, "EMD"),
    LAND(0, 3, "LAND");

    private final int min;
    private final int max;
    private final String type;

    /**
     * 줌 레벨 값으로 해당하는 ZoomLevel 타입을 반환하는 팩토리 메서드
     * @param zoomLevel 줌 레벨 값
     * @return 해당하는 ZoomLevel 타입
     * @throws IllegalArgumentException 유효하지 않은 줌 레벨인 경우
     */
    public static ZoomLevel from(int zoomLevel) {
        for (ZoomLevel level : values()) {
            if (zoomLevel >= level.min && zoomLevel <= level.max) {
                return level;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 줌 레벨입니다: " + zoomLevel);
    }
}
