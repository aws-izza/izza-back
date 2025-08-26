package com.izza.analysis.service;

import com.izza.analysis.vo.AnalysisStatisticsType;
import com.izza.analysis.service.dto.LandAnalysisData;
import org.springframework.stereotype.Component;

/**
 * 전기선 개수 점수 계산기
 * 전기선이 많을수록 높은 점수 (전력 인프라 충실성 향상)
 */
@Component
public class TransmissionLineCountScoreCalculator implements ScoreCalculator {
    
    private static final double BASE_SCORE = 0.5;
    
    @Override
    public double calculateScore(LandAnalysisData request) {
        int transmissionLineCount = request.getTransmissionLineCount();
        var statisticsRange = request.getStatisticsRanges().get(AnalysisStatisticsType.TRANSMISSION_LINE_COUNT);
        
        long min = statisticsRange.min();
        long max = statisticsRange.max();
        
        if (max == min) {
            return BASE_SCORE;
        }
        
        // 정규화 점수 계산: 전기선이 많을수록 높은 점수
        double score = BASE_SCORE + (transmissionLineCount - min) / (double)(max - min) * (1 - BASE_SCORE);
        
        // 0~1 범위로 제한
        return Math.max(0.0, Math.min(1.0, score));
    }
    
    @Override
    public AnalysisStatisticsType getStatisticsType() {
        return AnalysisStatisticsType.TRANSMISSION_LINE_COUNT;
    }
}