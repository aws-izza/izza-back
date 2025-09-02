package com.izza.analysis.service;

import com.izza.analysis.service.dto.LandAnalysisData;
import com.izza.analysis.service.dto.ScoreResult;
import com.izza.analysis.vo.AnalysisStatisticsType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 재난문자 발송 건수 점수 계산기
 * README.md 기준: (최대값 - x) / (최대값 - 최소값) - 적을수록 유리
 * 재난문자가 적을수록 더 높은 점수를 받는 역정규화 방식
 */
@Service
@Slf4j
public class DisasterCountScoreCalculator implements ScoreCalculator {

    private static final double BASE_SCORE = 0.5;

    @Override
    public String getCalculatorName() {
        return "DisasterCountScoreCalculator";
    }

    @Override
    public AnalysisStatisticsType getStatisticsType() {
        return AnalysisStatisticsType.DISASTER_COUNT;
    }

    @Override
    public ScoreResult calculateScore(LandAnalysisData data) {
        // 1. 실제 값 가져오기 (재난문자 발송 건수)
        AnalysisStatisticsType statisticsType = getStatisticsType();
        if (!data.getStatisticsRanges().containsKey(statisticsType)) {
            return null;
        }


        double actualValue = data.getEmergencyTextInfo().totalDisasterCount();
        
        // 2. 통계 범위 가져오기
        var statisticsRange = data.getStatisticsRanges().get(getStatisticsType());
        long min = statisticsRange.min();
        long max = statisticsRange.max();
        
        // 3. 원본 점수 계산 (역정규화: 적을수록 유리)
        double originalScore = calculateReverseNormalizedScore(actualValue, min, max);
        
        // 4. 가중치 적용
        Double categoryWeight = data.getCategoryNormalizedWeights().get(statisticsType);
        Double globalWeight = data.getGlobalNormalizedWeights().get(statisticsType);
        
        double categoryNormalizedScore = categoryWeight != null ? 
            originalScore * (categoryWeight / 100.0) : originalScore;
        double globalNormalizedScore = globalWeight != null ? 
            originalScore * (globalWeight / 100.0) : originalScore;
        
        log.debug("재난문자 점수 계산 완료 - 실제값: {}, 범위: [{}, {}], 원본점수: {}", 
                actualValue, min, max, originalScore);
        
        return ScoreResult.builder()
                .statisticsType(statisticsType)
                .originalScore(originalScore)
                .categoryNormalizedScore(categoryNormalizedScore)
                .globalNormalizedScore(globalNormalizedScore)
                .build();
    }

    /**
     * 역정규화 점수 계산
     * 재난문자가 적을수록 높은 점수를 받는 방식
     * 공식: (최대값 - x) / (최대값 - 최소값)
     */
    private double calculateReverseNormalizedScore(double actualValue, long min, long max) {
        if (max == min) {
            return BASE_SCORE;
        }
        
        // 역정규화: 값이 작을수록 높은 점수
        double normalizedScore = (max - actualValue) / (double)(max - min);
        
        // 0~1 범위로 제한
        return Math.max(0.0, Math.min(1.0, normalizedScore));
    }
}