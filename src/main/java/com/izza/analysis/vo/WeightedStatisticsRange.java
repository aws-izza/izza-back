package com.izza.analysis.vo;

import lombok.Builder;

/**
 * 가중치가 포함된 통계 범위 정보 VO
 * 분석 시 사용되는 최솟값, 최댓값, 가중치를 포함
 *
 * @param min    최솟값 (null 가능)
 * @param max    최댓값 (null 가능)
 * @param weight 가중치 (null 가능, 0 ~ 100)
 */
@Builder
public record WeightedStatisticsRange(Long min, Long max, Integer weight) {

    /**
     * 가중치가 포함된 통계 범위 생성
     */
    public static WeightedStatisticsRange of(Long min, Long max, Integer weight) {
        return WeightedStatisticsRange.builder()
                .min(min)
                .max(max)
                .weight(weight)
                .build();
    }

    /**
     * 기본 가중치(100)로 통계 범위 생성
     */
    public static WeightedStatisticsRange of(Long min, Long max) {
        return of(min, max, 100);
    }

    /**
     * 범위 내에 값이 포함되는지 확인
     */
    public boolean contains(long value) {
        if (min == null || max == null) {
            return false;
        }
        return value >= min && value <= max;
    }

    /**
     * 값을 0.0 ~ 1.0 범위로 정규화
     */
    public double normalize(long value) {
        if (min == null || max == null) {
            return 0.0; // null인 경우 기본값
        }
        
        if (max.equals(min)) {
            return 1.0; // 범위가 없는 경우 최대값으로 처리
        }

        // min ~ max 범위를 0.0 ~ 1.0으로 정규화
        double normalized = (double) (value - min) / (max - min);
        return Math.max(0.0, Math.min(1.0, normalized));
    }

    /**
     * 가중치가 적용된 정규화 점수 계산
     */
    public double calculateWeightedScore(long value) {
        if (weight == null) {
            return normalize(value); // 가중치가 null인 경우 정규화 점수만 반환
        }
        return normalize(value) * (weight / 100.0); // 가중치를 0~1 범위로 변환
    }
}