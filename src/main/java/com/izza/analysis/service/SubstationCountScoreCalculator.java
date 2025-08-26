package com.izza.analysis.service;

import com.izza.analysis.vo.AnalysisStatisticsType;
import com.izza.analysis.service.dto.LandAnalysisData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 변전소 개수 점수 계산기
 * 변전소가 많을수록 높은 점수 (전력 공급 안정성 향상)
 */
@Component
@Slf4j
public class SubstationCountScoreCalculator implements ScoreCalculator {
    
    private static final double BASE_SCORE = 0.5;
    
    @Override
    public double calculateScore(LandAnalysisData request) {
        int substationCount = request.getSubstationCount();
        var statisticsRange = request.getStatisticsRanges().get(AnalysisStatisticsType.SUBSTATION_COUNT);
        
        long min = statisticsRange.min();
        long max = statisticsRange.max();
        
        if (max == min) {
            return BASE_SCORE;
        }
        
        // 정규화 점수 계산: 변전소가 많을수록 높은 점수
        // base_score + (x - min) / (max - min) * (1 - base_score)
        double score = BASE_SCORE + (substationCount - min) / (double)(max - min) * (1 - BASE_SCORE);
        
        // 0~1 범위로 제한
        return Math.max(0.0, Math.min(1.0, score));
    }
    
    @Override
    public AnalysisStatisticsType getStatisticsType() {
        return AnalysisStatisticsType.SUBSTATION_COUNT;
    }
}