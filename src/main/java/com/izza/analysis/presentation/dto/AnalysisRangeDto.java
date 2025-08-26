package com.izza.analysis.presentation.dto;

/**
 * 분석 모듈에서 사용하는 범위 정보 DTO
 * search 도메인의 LongRangeDto와 분리하여 도메인 독립성 확보
 *
 * @param min 최솟값
 * @param max 최댓값
 */
public record AnalysisRangeDto(long min, long max) {

    /**
     * 정적 팩토리 메서드
     */
    public static AnalysisRangeDto of(long min, long max) {
        return new AnalysisRangeDto(min, max);
    }
}