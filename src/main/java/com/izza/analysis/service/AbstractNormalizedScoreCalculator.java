package com.izza.analysis.service;

import com.izza.analysis.vo.AnalysisStatisticsType;
import com.izza.analysis.service.dto.LandAnalysisData;
import com.izza.analysis.service.dto.ScoreResult;

/**
 * 정규분포 점수 계산기의 공통 로직을 담은 추상 클래스
 * 값이 클수록 높은 점수를 받는 선형 정규화 방식을 사용
 */
public abstract class AbstractNormalizedScoreCalculator implements ScoreCalculator {
    
    @Override
    public final ScoreResult calculateScore(LandAnalysisData data) {
        // 0. statisticsRanges에 해당 타입이 없으면 점수 계산하지 않음
        AnalysisStatisticsType statisticsType = getStatisticsType();

        if (!data.getStatisticsRanges().containsKey(statisticsType)) {
            return null;
        }


        // 1. 각 구현체에서 실제 값을 가져옴
        double actualValue = getActualValue(data);
        
        // 2. 통계 범위 가져오기
        var statisticsRange = data.getStatisticsRanges().get(statisticsType);
        long min = statisticsRange.min();
        long max = statisticsRange.max();
        
        // 3. 원본 점수 계산
        double originalScore = calculateNormalizedScore(actualValue, min, max);
        
        // 4. 가중치 적용
        Double categoryWeight = data.getCategoryNormalizedWeights().get(statisticsType);
        Double globalWeight = data.getGlobalNormalizedWeights().get(statisticsType);
        
        double categoryNormalizedScore = categoryWeight != null ? 
            originalScore * (categoryWeight / 100.0) : originalScore;
        double globalNormalizedScore = globalWeight != null ? 
            originalScore * (globalWeight / 100.0) : originalScore;
        
        return ScoreResult.builder()
                .statisticsType(statisticsType)
                .originalScore(originalScore)
                .categoryNormalizedScore(categoryNormalizedScore)
                .globalNormalizedScore(globalNormalizedScore)
                .build();
    }
    
    /**
     * 정규화 점수 계산 (공통 로직)
     * 값이 클수록 높은 점수를 받는 방식
     */
    protected double calculateNormalizedScore(double actualValue, long min, long max) {
        double baseScore = getBaseScore();
        
        if (max == min) {
            return baseScore;
        }
        
        // 정규화 점수 계산: 값이 클수록 높은 점수
        double normalizedScore = baseScore + (actualValue - min) / (double)(max - min) * (1 - baseScore);
        
        // 0~1 범위로 제한
        return Math.max(0.0, Math.min(1.0, normalizedScore));
    }
    
    /**
     * 각 구현체에서 실제 측정값을 반환
     * @param data 토지 분석 데이터
     * @return 해당 지표의 실제 측정값
     */
    protected abstract double getActualValue(LandAnalysisData data);
    
    /**
     * 각 구현체별 기준 점수를 반환
     * @return 기준 점수 (0.0 ~ 1.0)
     */
    protected abstract double getBaseScore();
}