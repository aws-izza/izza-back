package com.izza.analysis.service;

import com.izza.analysis.vo.AnalysisStatisticsType;
import com.izza.analysis.service.dto.LandAnalysisData;
import org.springframework.stereotype.Component;

/**
 * 송전탑 개수 점수 계산기
 * 송전탑이 많을수록 높은 점수 (전력 전송 인프라 충실성 향상)
 */
@Component
public class TransmissionTowerCountScoreCalculator implements ScoreCalculator {
    
    private static final double BASE_SCORE = 0.5;
    
    @Override
    public double calculateScore(LandAnalysisData request) {
        int transmissionTowerCount = request.getTransmissionTowerCount();
        var statisticsRange = request.getStatisticsRanges().get(AnalysisStatisticsType.TRANSMISSION_TOWER_COUNT);
        
        long min = statisticsRange.min();
        long max = statisticsRange.max();
        
        if (max == min) {
            return BASE_SCORE;
        }
        
        // 정규화 점수 계산: 송전탑이 많을수록 높은 점수
        double score = BASE_SCORE + (transmissionTowerCount - min) / (double)(max - min) * (1 - BASE_SCORE);
        
        // 0~1 범위로 제한
        return Math.max(0.0, Math.min(1.0, score));
    }
    
    @Override
    public AnalysisStatisticsType getStatisticsType() {
        return AnalysisStatisticsType.TRANSMISSION_TOWER_COUNT;
    }
}